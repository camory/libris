import type { AppUpdate } from "../application/AppUpdate";

export class FakeAppUpdate implements AppUpdate {
  onNewVersion(): void {}

  install(): void {}
}
