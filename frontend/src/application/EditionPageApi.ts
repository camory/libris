import type { InjectionKey } from "vue";
import type { Book } from "../domain/Book";

export type FindBookAnswer =
  { outcome: "found"; book: Book } | { outcome: "problem"; type: string };

export interface EditionPageApi {
  find(id: string): Promise<FindBookAnswer>;
}

export const editionPageApiKey: InjectionKey<EditionPageApi> =
  Symbol("EditionPageApi");
