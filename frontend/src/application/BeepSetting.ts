import type { InjectionKey } from "vue";

export interface BeepSetting {
  isOn(): boolean;
  keep(on: boolean): void;
}

export const beepSettingKey: InjectionKey<BeepSetting> = Symbol("BeepSetting");
