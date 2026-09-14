import type { BarcodeScanner } from "../../application/BarcodeScanner";

interface BarcodeDetectorConstructor {
  new (options: { formats: string[] }): BarcodeDetector;
  getSupportedFormats(): Promise<string[]>;
}

interface BarcodeDetector {
  detect(source: CanvasImageSource): Promise<{ rawValue: string }[]>;
}

const format = "ean_13";

function detector(): BarcodeDetectorConstructor | undefined {
  return (globalThis as { BarcodeDetector?: BarcodeDetectorConstructor })
    .BarcodeDetector;
}

export class CameraBarcodeScanner implements BarcodeScanner {
  async isAvailable(): Promise<boolean> {
    const formats = await detector()?.getSupportedFormats();
    return formats?.includes(format) === true;
  }

  read(): Promise<string | null> {
    return Promise.resolve(null);
  }

  stop(): void {}
}
