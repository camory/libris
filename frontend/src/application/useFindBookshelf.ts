import { ref, type Ref } from "vue";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Member } from "../domain/Member";
import type { BookshelvesApi, FindAnswer } from "./BookshelvesApi";

export type FoundBookshelfState =
  | { status: "loading" }
  | { status: "found"; bookshelf: Bookshelf; members: Member[] }
  | { status: "notFound" }
  | { status: "unavailable" };

export function useFindBookshelf(bookshelvesApi: BookshelvesApi): {
  state: Ref<FoundBookshelfState>;
  find: (id: string) => Promise<void>;
} {
  const state = ref<FoundBookshelfState>({ status: "loading" });

  async function find(id: string) {
    state.value = stateOf(await bookshelvesApi.find(id));
  }

  return { state, find };
}

function stateOf(answer: FindAnswer): FoundBookshelfState {
  if (answer.outcome === "found")
    return {
      status: "found",
      bookshelf: answer.bookshelf,
      members: answer.members,
    };
  switch (answer.type) {
    case "/problems/not-found":
      return { status: "notFound" };
    default:
      return { status: "unavailable" };
  }
}
