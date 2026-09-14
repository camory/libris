import type { BarcodeScanner } from "../application/BarcodeScanner";

export class FakeBarcodeScanner implements BarcodeScanner {
  readonly readsInto: HTMLVideoElement[] = [];
  stops = 0;
  private pending?: (code: string | null) => void;

  constructor(
    private readonly available: boolean,
    private readonly code: string | null | Promise<string | null> = null,
  ) {}

  isAvailable(): Promise<boolean> {
    return Promise.resolve(this.available);
  }

  read(into: HTMLVideoElement): Promise<string | null> {
    this.readsInto.push(into);
    return Promise.race([
      Promise.resolve(this.code),
      new Promise<string | null>((resolve) => {
        this.pending = resolve;
      }),
    ]);
  }

  stop(): void {
    this.stops += 1;
    const resolve = this.pending;
    this.pending = undefined;
    resolve?.(null);
  }
}
