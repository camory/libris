import { ref, type Ref } from "vue";
import type { Book } from "../domain/Book";
import type { CatalogueApi } from "./CatalogueApi";

export type CatalogueState =
  | { status: "loading" }
  | { status: "empty" }
  | { status: "listed"; books: Book[] }
  | { status: "loadingMore"; books: Book[] }
  | { status: "unavailable" };

export function useBrowseCatalogue(catalogueApi: CatalogueApi): {
  state: Ref<CatalogueState>;
  browse: () => Promise<void>;
} {
  const state = ref<CatalogueState>({ status: "loading" });
  let next: string | null = null;

  async function browse() {
    if (state.value.status !== "loading" && next === null) return;
    if (state.value.status === "listed")
      state.value = { status: "loadingMore", books: state.value.books };
    try {
      const page = await catalogueApi.browse(next);
      next = page.next;
      const books = [
        ...(state.value.status === "loadingMore" ? state.value.books : []),
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
