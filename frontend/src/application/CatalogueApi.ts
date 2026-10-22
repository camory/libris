import type { InjectionKey } from "vue";
import type { Book } from "../domain/Book";

export interface BookPage {
  books: Book[];
  next: string | null;
}

export type FindBookAnswer =
  { outcome: "found"; book: Book } | { outcome: "problem"; type: string };

export interface CatalogueApi {
  browse(after: string | null): Promise<BookPage>;
  find(id: string): Promise<FindBookAnswer>;
}

export const catalogueApiKey: InjectionKey<CatalogueApi> =
  Symbol("CatalogueApi");
