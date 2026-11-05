import type { InjectionKey } from "vue";

export type RemoveAnswer =
  { outcome: "removed" } | { outcome: "problem"; type: string };

export interface CopiesApi {
  remove(copyId: string): Promise<RemoveAnswer>;
}

export const copiesApiKey: InjectionKey<CopiesApi> = Symbol("CopiesApi");
