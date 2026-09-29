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
  let next: string | null = null;

  async function browse() {
    try {
      const page = await catalogueApi.browse(next);
      next = page.next;
      const books = [
        ...(state.value.status === "listed" ? state.value.books : []),
        ...page.books,
      ];
      state.value =
        books.length === 0 ? { status: "empty" } : { status: "listed", books };
    } catch {
      state.value = { status: "unavailable" };
    }
  }

  return { state, browse };
}
