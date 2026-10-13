import { ref, type Ref } from "vue";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Member } from "../domain/Member";
import type { BookshelvesApi } from "./BookshelvesApi";

export type FoundBookshelfState =
  | { status: "loading" }
  | { status: "found"; bookshelf: Bookshelf; members: Member[] }
  | { status: "unavailable" };

export function useFindBookshelf(bookshelvesApi: BookshelvesApi): {
  state: Ref<FoundBookshelfState>;
  find: (id: string) => Promise<void>;
} {
  const state = ref<FoundBookshelfState>({ status: "loading" });

  async function find(id: string) {
    const answer = await bookshelvesApi.find(id);
    if (answer.outcome === "found")
      state.value = {
        status: "found",
        bookshelf: answer.bookshelf,
        members: answer.members,
      };
    else state.value = { status: "unavailable" };
  }

  return { state, find };
}
