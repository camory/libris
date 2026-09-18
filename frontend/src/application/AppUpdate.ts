import type { InjectionKey } from "vue";

export interface AppUpdate {
  onNewVersion(announce: () => void): void;
  install(): void;
}

export const appUpdateKey: InjectionKey<AppUpdate> = Symbol("AppUpdate");
