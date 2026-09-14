import type { BarcodeScanner } from "../../application/BarcodeScanner";
import { isbn13Of } from "../../domain/Isbn13";

interface BarcodeDetectorConstructor {
  new (options: { formats: string[] }): BarcodeDetector;
  getSupportedFormats(): Promise<string[]>;
}

interface BarcodeDetector {
  detect(source: CanvasImageSource): Promise<{ rawValue: string }[]>;
}

const format = "ean_13";
const betweenLooks = 100;

async function open(): Promise<MediaStream | null> {
  try {
    return await navigator.mediaDevices.getUserMedia({
      video: { facingMode: "environment" },
    });
  } catch {
    return null;
  }
}

function pause(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, betweenLooks));
}

function detector(): BarcodeDetectorConstructor | undefined {
  return (globalThis as { BarcodeDetector?: BarcodeDetectorConstructor })
    .BarcodeDetector;
}

export class CameraBarcodeScanner implements BarcodeScanner {
  async isAvailable(): Promise<boolean> {
    const formats = await detector()?.getSupportedFormats();
    return formats?.includes(format) === true;
  }

  async read(into: HTMLVideoElement): Promise<string | null> {
    const stream = await open();
    if (stream === null) {
      return null;
    }
    into.srcObject = stream;
    await into.play();
    const barcodes = new (detector()!)({ formats: [format] });
    for (;;) {
      for (const { rawValue } of await barcodes.detect(into)) {
        const isbn13 = isbn13Of(rawValue);
        if (isbn13 !== null) {
          return isbn13;
        }
      }
      await pause();
    }
  }

  stop(): void {}
}
