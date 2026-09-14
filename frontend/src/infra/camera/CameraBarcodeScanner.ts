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

function pause(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, betweenLooks));
}

function look(
  barcodes: BarcodeDetector,
  into: HTMLVideoElement,
): Promise<{ rawValue: string }[]> {
  return barcodes.detect(into).catch(() => []);
}

function firstIsbnAmong(seen: { rawValue: string }[]): string | null {
  for (const { rawValue } of seen) {
    const isbn13 = isbn13Of(rawValue);
    if (isbn13 !== null) {
      return isbn13;
    }
  }
  return null;
}

function detector(): BarcodeDetectorConstructor | undefined {
  return (globalThis as { BarcodeDetector?: BarcodeDetectorConstructor })
    .BarcodeDetector;
}

export class CameraBarcodeScanner implements BarcodeScanner {
  private stream: MediaStream | null = null;
  private looking = false;

  async isAvailable(): Promise<boolean> {
    const formats = await detector()?.getSupportedFormats();
    return formats?.includes(format) === true;
  }

  async read(into: HTMLVideoElement): Promise<string | null> {
    this.looking = true;
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: "environment" },
      });
      into.srcObject = this.stream;
      await into.play();
      const barcodes = new (detector()!)({ formats: [format] });
      while (this.looking) {
        const isbn13 = firstIsbnAmong(await look(barcodes, into));
        if (isbn13 !== null && this.looking) {
          return isbn13;
        }
        await pause();
      }
      return null;
    } catch {
      return null;
    } finally {
      this.release();
    }
  }

  stop(): void {
    this.release();
  }

  private release(): void {
    this.looking = false;
    const stream = this.stream;
    this.stream = null;
    stream?.getTracks().forEach((track) => track.stop());
  }
}
