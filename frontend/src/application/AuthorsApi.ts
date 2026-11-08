import type { InjectionKey } from "vue";

export interface AuthorsApi {
  names(): Promise<string[]>;
}

export const authorsApiKey: InjectionKey<AuthorsApi> = Symbol("AuthorsApi");
