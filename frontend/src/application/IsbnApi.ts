import type { InjectionKey } from "vue";
import type { SourceEdition } from "../domain/SourceEdition";

export type IsbnAnswer =
  | { outcome: "found"; edition: SourceEdition }
  | { outcome: "problem"; type: string };

export interface IsbnApi {
  lookUp(isbn13: string): Promise<IsbnAnswer>;
}

export const isbnApiKey: InjectionKey<IsbnApi> = Symbol("IsbnApi");
