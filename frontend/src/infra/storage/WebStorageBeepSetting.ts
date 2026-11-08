import type { BeepSetting } from "../../application/BeepSetting";

export class WebStorageBeepSetting implements BeepSetting {
  constructor(private readonly storage: Storage) {}

  isOn(): boolean {
    return false;
  }

  keep(): void {
    this.storage.setItem("beep", "on");
  }
}
