import type { BarcodeScanner } from "../../application/BarcodeScanner";

interface BarcodeDetectorConstructor {
  new (options: { formats: string[] }): BarcodeDetector;
  getSupportedFormats(): Promise<string[]>;
}

interface BarcodeDetector {
  detect(source: CanvasImageSource): Promise<{ rawValue: string }[]>;
}

function detector(): BarcodeDetectorConstructor | undefined {
  return (globalThis as { BarcodeDetector?: BarcodeDetectorConstructor })
    .BarcodeDetector;
}

export class CameraBarcodeScanner implements BarcodeScanner {
  isAvailable(): Promise<boolean> {
    return Promise.resolve(detector() !== undefined);
  }

  read(): Promise<string | null> {
    return Promise.resolve(null);
  }

  stop(): void {}
}
