import type { BarcodeScanner } from "../../application/BarcodeScanner";

export class CameraBarcodeScanner implements BarcodeScanner {
  isAvailable(): Promise<boolean> {
    return Promise.resolve(false);
  }

  read(): Promise<string | null> {
    return Promise.resolve(null);
  }

  stop(): void {}
}
