import { ref, type Ref } from "vue";
import type { Book } from "../domain/Book";
import { searchQueryOf } from "../domain/SearchQuery";
import type { SearchApi, SearchPage } from "./SearchApi";

export type SearchState =
  | { status: "ready" }
  | { status: "found"; books: Book[]; loadingMore: boolean }
  | { status: "near"; query: string; books: Book[]; loadingMore: boolean }
  | { status: "nothing"; query: string };

export function useSearchCatalogue(searchApi: SearchApi): {
  state: Ref<SearchState>;
  search: (text: string) => Promise<void>;
  searchMore: () => Promise<void>;
} {
  const state = ref<SearchState>({ status: "ready" });
  let searched = "";
  let next: string | null = null;

  async function search(text: string) {
    const query = searchQueryOf(text);
    if (query === null) {
      state.value = { status: "ready" };
      return;
    }
    const page = await searchApi.search(query, null);
    searched = query;
    next = page.next;
    state.value = stateOf(query, page);
  }

  async function searchMore() {
    const shown = state.value;
    if (shown.status !== "found" && shown.status !== "near") return;
    if (next === null) return;
    state.value = { ...shown, loadingMore: true };
    const page = await searchApi.search(searched, next);
    next = page.next;
    state.value = stateOf(searched, {
      ...page,
      books: [...shown.books, ...page.books],
    });
  }

  return { state, search, searchMore };
}

function stateOf(query: string, page: SearchPage): SearchState {
  if (page.books.length === 0) return { status: "nothing", query };
  if (page.near)
    return { status: "near", query, books: page.books, loadingMore: false };
  return { status: "found", books: page.books, loadingMore: false };
}
