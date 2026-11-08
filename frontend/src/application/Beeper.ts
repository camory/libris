import type { InjectionKey } from "vue";

export interface Beeper {
  beep(): void;
  ready(): void;
}

export const beeperKey: InjectionKey<Beeper> = Symbol("Beeper");
