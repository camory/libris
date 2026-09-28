import { ref, type Ref } from "vue";
import type { Book } from "../domain/Book";
import type { CatalogueApi } from "./CatalogueApi";

export type CatalogueState =
  | { status: "loading" }
  | { status: "empty" }
  | { status: "listed"; books: Book[] };

export function useBrowseCatalogue(catalogueApi: CatalogueApi): {
  state: Ref<CatalogueState>;
  browse: () => Promise<void>;
} {
  const state = ref<CatalogueState>({ status: "loading" });

  async function browse() {
    const page = await catalogueApi.browse(null);
    if (page.books.length === 0) {
      state.value = { status: "empty" };
    } else {
      state.value = { status: "listed", books: page.books };
    }
  }

  return { state, browse };
}
