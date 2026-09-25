import type { InjectionKey } from "vue";
import type { Copy } from "../domain/Copy";
import type { SourceEdition } from "../domain/SourceEdition";

export type AddAnswer =
  { outcome: "added"; copy: Copy } | { outcome: "problem"; type: string };

export interface BookshelfApi {
  add(bookshelfId: string, edition: SourceEdition): Promise<AddAnswer>;
}

export const bookshelfApiKey: InjectionKey<BookshelfApi> =
  Symbol("BookshelfApi");
