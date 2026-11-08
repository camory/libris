import type { BeepSetting } from "../../application/BeepSetting";

export class WebStorageBeepSetting implements BeepSetting {
  constructor(private readonly storage: Storage) {}

  isOn(): boolean {
    return this.storage.getItem("beep") === "on";
  }

  keep(on: boolean): void {
    if (on) {
      this.storage.setItem("beep", "on");
    } else {
      this.storage.removeItem("beep");
    }
  }
}
