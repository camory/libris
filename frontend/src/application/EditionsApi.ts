import type { InjectionKey } from "vue";
import type { Book } from "../domain/Book";
import type { SourceEdition } from "../domain/SourceEdition";

export type EditionDetails = Omit<SourceEdition, "isbn13">;

export type SaveAnswer =
  { outcome: "saved"; book: Book } | { outcome: "problem"; type: string };

export interface EditionsApi {
  save(id: string, edition: EditionDetails): Promise<SaveAnswer>;
}

export const editionsApiKey: InjectionKey<EditionsApi> = Symbol("EditionsApi");
