import { ref, type Ref } from "vue";
import type { Book } from "../domain/Book";
import { searchQueryOf } from "../domain/SearchQuery";
import type { SearchApi, SearchPage } from "./SearchApi";

export type SearchState =
  | { status: "ready" }
  | { status: "found"; books: Book[] }
  | { status: "near"; query: string; books: Book[] }
  | { status: "nothing"; query: string };

export function useSearchCatalogue(searchApi: SearchApi): {
  state: Ref<SearchState>;
  search: (text: string) => Promise<void>;
} {
  const state = ref<SearchState>({ status: "ready" });

  async function search(text: string) {
    const query = searchQueryOf(text);
    if (query === null) {
      state.value = { status: "ready" };
      return;
    }
    const page = await searchApi.search(query, null);
    state.value = stateOf(query, page);
  }

  return { state, search };
}

function stateOf(query: string, page: SearchPage): SearchState {
  if (page.books.length === 0) return { status: "nothing", query };
  if (page.near) return { status: "near", query, books: page.books };
  return { status: "found", books: page.books };
}
