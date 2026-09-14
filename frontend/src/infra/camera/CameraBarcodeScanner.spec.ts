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

  function detectorAnnouncing(formats: string[], ...codes: string[]) {
    let reads = 0;
    vi.stubGlobal(
      "BarcodeDetector",
      class {
        static getSupportedFormats() {
          return Promise.resolve(formats);
        }
        detect() {
          const code = codes[Math.min(reads++, codes.length - 1)];
          return Promise.resolve(code === undefined ? [] : [{ rawValue: code }]);
        }
      },
    );
  }
});
