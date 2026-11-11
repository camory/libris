import type { InjectionKey } from "vue";
import type { BookPage } from "./CatalogueApi";

export interface SearchPage extends BookPage {
  near: boolean;
}

export interface SearchApi {
  search(query: string, after: string | null): Promise<SearchPage>;
}

export const searchApiKey: InjectionKey<SearchApi> = Symbol("SearchApi");
