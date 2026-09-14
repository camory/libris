import type { InjectionKey } from "vue";

export interface BarcodeScanner {
  isAvailable(): Promise<boolean>;
  read(into: HTMLVideoElement): Promise<string | null>;
  stop(): void;
}

export const barcodeScannerKey: InjectionKey<BarcodeScanner> =
  Symbol("BarcodeScanner");
