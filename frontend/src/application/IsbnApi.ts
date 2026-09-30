import type { InjectionKey } from "vue";
import type { Copy } from "../domain/Copy";
import type { CoverCandidate } from "../domain/Cover";
import type { SourceEdition } from "../domain/SourceEdition";

export type IsbnAnswer =
  | {
      outcome: "found";
      edition: SourceEdition;
      copies: Copy[];
      covers?: CoverCandidate[];
    }
  | { outcome: "problem"; type: string };

export interface IsbnApi {
  lookUp(isbn13: string): Promise<IsbnAnswer>;
}

export const isbnApiKey: InjectionKey<IsbnApi> = Symbol("IsbnApi");
