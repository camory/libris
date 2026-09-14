import { afterEach, describe, expect, it, vi } from "vitest";
import { CameraBarcodeScanner } from "./CameraBarcodeScanner";

describe("CameraBarcodeScanner", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
    Reflect.deleteProperty(navigator, "mediaDevices");
  });

  it("is unavailable where the browser announces no barcode detector", async () => {
    expect(await new CameraBarcodeScanner().isAvailable()).toBe(false);
  });

  it("is available where the detector lists the EAN-13 format", async () => {
    // Given
    detectorAnnouncing(["ean_13"]);

    // When
    const available = await new CameraBarcodeScanner().isAvailable();

    // Then
    expect(available).toBe(true);
  });

  it("is unavailable where the detector does not read EAN-13", async () => {
    // Given
    detectorAnnouncing(["qr_code"]);

    // When
    const available = await new CameraBarcodeScanner().isAvailable();

    // Then
    expect(available).toBe(false);
  });

  it("reads the ISBN the camera shows and gives the camera back", async () => {
    // Given
    detectorAnnouncing(["ean_13"], "9782723488525");
    const camera = cameraAllowed();

    // When
    const read = await new CameraBarcodeScanner().read(video());

    // Then
    expect(read).toBe("9782723488525");
    expect(camera.stops).toBe(1);
  });

  it("gives the camera back and reads nothing where the picture cannot start", async () => {
    // Given
    detectorAnnouncing(["ean_13"], "9782723488525");
    const camera = cameraAllowed();
    vi.spyOn(HTMLMediaElement.prototype, "play").mockRejectedValue(
      new Error("aborted"),
    );

    // When
    const read = await new CameraBarcodeScanner().read(video());

    // Then
    expect(read).toBeNull();
    expect(camera.stops).toBe(1);
  });

  it("keeps looking past a code that is not an ISBN", async () => {
    // Given
    detectorAnnouncing(["ean_13"], "1234567890128", "9782723488525");
    cameraAllowed();

    // When
    const read = await new CameraBarcodeScanner().read(video());

    // Then
    expect(read).toBe("9782723488525");
  });

  it("keeps looking past a look the detector could not take", async () => {
    // Given
    detectorAnnouncing(["ean_13"], new Error("no frame yet"), "9782723488525");
    cameraAllowed();

    // When
    const read = await new CameraBarcodeScanner().read(video());

    // Then
    expect(read).toBe("9782723488525");
  });

  it("reads nothing where the reader refuses the camera", async () => {
    // Given
    detectorAnnouncing(["ean_13"], "9782723488525");
    cameraRefused();

    // When
    const read = await new CameraBarcodeScanner().read(video());

    // Then
    expect(read).toBeNull();
  });

  it("gives the camera back and reads nothing once stopped", async () => {
    // Given
    detectorAnnouncing(["ean_13"]);
    const camera = cameraAllowed();
    const scanner = new CameraBarcodeScanner();
    const read = scanner.read(video());
    await tick();

    // When
    scanner.stop();

    // Then
    expect(await read).toBeNull();
    expect(camera.stops).toBe(1);
  });

  it("reads nothing once stopped, even with a code in the frame", async () => {
    // Given
    let seen!: (code: string) => void;
    detectorAnnouncing(
      ["ean_13"],
      new Promise<string>((resolve) => {
        seen = resolve;
      }),
    );
    const camera = cameraAllowed();
    const scanner = new CameraBarcodeScanner();
    const read = scanner.read(video());
    await tick();

    // When
    scanner.stop();
    seen("9782723488525");

    // Then
    expect(await read).toBeNull();
    expect(camera.stops).toBe(1);
  });

  function tick() {
    return new Promise((resolve) => setTimeout(resolve, 0));
  }

  function video() {
    return document.createElement("video");
  }

  function cameraAllowed() {
    const camera = { stops: 0 };
    const track = {
      stop: () => {
        camera.stops += 1;
      },
    };
    const stream = { getTracks: () => [track] };
    Object.defineProperty(navigator, "mediaDevices", {
      configurable: true,
      value: { getUserMedia: () => Promise.resolve(stream) },
    });
    vi.spyOn(HTMLMediaElement.prototype, "play").mockResolvedValue();
    return camera;
  }

  function cameraRefused() {
    Object.defineProperty(navigator, "mediaDevices", {
      configurable: true,
      value: { getUserMedia: () => Promise.reject(new Error("refused")) },
    });
  }

  function detectorAnnouncing(
    formats: string[],
    ...looks: (string | Error | Promise<string>)[]
  ) {
    let reads = 0;
    vi.stubGlobal(
      "BarcodeDetector",
      class {
        static getSupportedFormats() {
          return Promise.resolve(formats);
        }
        detect() {
          const look = looks[Math.min(reads++, looks.length - 1)];
          if (look instanceof Error) {
            return Promise.reject(look);
          }
          if (look === undefined) {
            return Promise.resolve([]);
          }
          return Promise.resolve(look).then((code) => [{ rawValue: code }]);
        }
      },
    );
  }
});
