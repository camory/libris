import { ref, type Ref } from "vue";
import type { Bookshelf } from "../domain/Bookshelf";
import type { KnownReader } from "../domain/KnownReader";
import type { Member } from "../domain/Member";
import type { BookshelvesApi, FindAnswer } from "./BookshelvesApi";
import type { ReadersApi } from "./ReadersApi";

export type FoundBookshelfState =
  | { status: "loading" }
  | {
      status: "found";
      bookshelf: Bookshelf;
      members: Member[];
      readers: KnownReader[];
    }
  | { status: "notFound" }
  | { status: "unavailable" };

export function useFindBookshelf(
  bookshelvesApi: BookshelvesApi,
  readersApi: ReadersApi,
): {
  state: Ref<FoundBookshelfState>;
  find: (id: string, readerId: string) => Promise<void>;
} {
  const state = ref<FoundBookshelfState>({ status: "loading" });

  async function find(id: string, readerId: string) {
    state.value = { status: "loading" };
    try {
      const answer = await bookshelvesApi.find(id);
      state.value = stateOf(answer, await readersFor(answer, readerId));
    } catch {
      state.value = { status: "unavailable" };
    }
  }

  async function readersFor(
    answer: FindAnswer,
    readerId: string,
  ): Promise<KnownReader[]> {
    const owns =
      answer.outcome === "found" &&
      answer.members.some(
        (member) => member.reader.id === readerId && member.role === "OWNER",
      );
    return owns ? readersApi.known() : [];
  }

  return { state, find };
}

function stateOf(
  answer: FindAnswer,
  readers: KnownReader[],
): FoundBookshelfState {
  if (answer.outcome === "found")
    return {
      status: "found",
      bookshelf: answer.bookshelf,
      members: answer.members,
      readers,
    };
  switch (answer.type) {
    case "/problems/not-found":
    case "/problems/validation":
      return { status: "notFound" };
    default:
      return { status: "unavailable" };
  }
}
