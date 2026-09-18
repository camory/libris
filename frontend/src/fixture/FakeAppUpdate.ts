import type { AppUpdate } from "../application/AppUpdate";

export class FakeAppUpdate implements AppUpdate {
  orders = 0;
  private listener: (() => void) | null = null;

  onNewVersion(announce: () => void): void {
    this.listener = announce;
  }

  install(): void {
    this.orders += 1;
  }

  announce(): void {
    this.listener?.();
  }
}
