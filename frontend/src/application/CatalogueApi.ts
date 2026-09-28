import type { InjectionKey } from "vue";
import type { Book } from "../domain/Book";

export interface BookPage {
  books: Book[];
  next: string | null;
}

export interface CatalogueApi {
  browse(after: string | null): Promise<BookPage>;
}

export const catalogueApiKey: InjectionKey<CatalogueApi> =
  Symbol("CatalogueApi");
