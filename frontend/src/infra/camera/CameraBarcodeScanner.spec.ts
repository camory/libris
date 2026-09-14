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
});
