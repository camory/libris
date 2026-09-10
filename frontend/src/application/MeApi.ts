import type { InjectionKey } from "vue";
import type { Reader } from "../domain/Reader";

export interface MeApi {
  currentReader(): Promise<Reader>;
}

export const meApiKey: InjectionKey<MeApi> = Symbol("MeApi");
