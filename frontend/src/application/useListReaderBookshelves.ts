import { ref, type Ref } from "vue";
import type { Membership } from "../domain/Membership";
import type { BookshelvesApi } from "./BookshelvesApi";

export type ReaderBookshelvesState =
  | { status: "loading" }
  | { status: "listed"; memberships: Membership[] }
  | { status: "unavailable" };

export function useListReaderBookshelves(bookshelvesApi: BookshelvesApi): {
  state: Ref<ReaderBookshelvesState>;
  list: () => Promise<void>;
} {
  const state = ref<ReaderBookshelvesState>({ status: "loading" });

  async function list() {
    state.value = {
      status: "listed",
      memberships: await bookshelvesApi.memberships(),
    };
  }

  return { state, list };
}
