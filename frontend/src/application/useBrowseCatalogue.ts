import { ref, type Ref } from "vue";
import type { Book } from "../domain/Book";
import type { CatalogueApi } from "./CatalogueApi";

export type CatalogueState =
  | { status: "loading" }
  | { status: "empty" }
  | { status: "listed"; books: Book[] }
  | { status: "unavailable" };

export function useBrowseCatalogue(catalogueApi: CatalogueApi): {
  state: Ref<CatalogueState>;
  browse: () => Promise<void>;
} {
  const state = ref<CatalogueState>({ status: "loading" });

  async function browse() {
    try {
      const page = await catalogueApi.browse(null);
      state.value =
        page.books.length === 0
          ? { status: "empty" }
          : { status: "listed", books: page.books };
    } catch {
      state.value = { status: "unavailable" };
    }
  }

  return { state, browse };
}
