import type { InjectionKey } from "vue";
import type { KnownReader } from "../domain/KnownReader";

export interface ReadersApi {
  known(): Promise<KnownReader[]>;
}

export const readersApiKey: InjectionKey<ReadersApi> = Symbol("ReadersApi");
