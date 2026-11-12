import { ref, type Ref } from "vue";
import type { Book } from "../domain/Book";
import { searchQueryOf } from "../domain/SearchQuery";
import type { SearchApi } from "./SearchApi";

export type SearchState =
  { status: "ready" } | { status: "found"; books: Book[] };

export function useSearchCatalogue(searchApi: SearchApi): {
  state: Ref<SearchState>;
  search: (text: string) => Promise<void>;
} {
  const state = ref<SearchState>({ status: "ready" });

  async function search(text: string) {
    const query = searchQueryOf(text);
    if (query === null) return;
    const page = await searchApi.search(query, null);
    state.value = { status: "found", books: page.books };
  }

  return { state, search };
}
