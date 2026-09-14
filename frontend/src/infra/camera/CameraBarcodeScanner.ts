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

function look(
  barcodes: BarcodeDetector,
  into: HTMLVideoElement,
): Promise<{ rawValue: string }[]> {
  return barcodes.detect(into).catch(() => []);
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
    this.stream = await open();
    if (this.stream === null) {
      return null;
    }
    into.srcObject = this.stream;
    try {
      await into.play();
    } catch {
      this.release();
      return null;
    }
    const barcodes = new (detector()!)({ formats: [format] });
    while (this.looking) {
      const seen = await look(barcodes, into);
      if (!this.looking) {
        break;
      }
      for (const { rawValue } of seen) {
        const isbn13 = isbn13Of(rawValue);
        if (isbn13 !== null) {
          this.release();
          return isbn13;
        }
      }
      await pause();
    }
    this.release();
    return null;
  }

  stop(): void {
    this.looking = false;
    this.release();
  }

  private release(): void {
    const stream = this.stream;
    this.stream = null;
    stream?.getTracks().forEach((track) => track.stop());
  }
}
