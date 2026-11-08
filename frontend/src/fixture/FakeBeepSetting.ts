import type { BeepSetting } from "../application/BeepSetting";

export class FakeBeepSetting implements BeepSetting {
  constructor(public on = false) {}

  isOn(): boolean {
    return this.on;
  }

  keep(on: boolean): void {
    this.on = on;
  }
}
