import { ref, type Ref } from "vue";
import type { BookshelvesApi } from "./BookshelvesApi";

export interface ChooseDefaultState {
  status: "ready" | "choosing" | "chosen" | "unavailable";
  bookshelfId: string | null;
  defaultBookshelfId: string | null;
}

export function useChooseDefaultBookshelf(bookshelvesApi: BookshelvesApi): {
  state: Ref<ChooseDefaultState>;
  choose: (bookshelfId: string) => Promise<void>;
} {
  const state = ref<ChooseDefaultState>({
    status: "ready",
    bookshelfId: null,
    defaultBookshelfId: null,
  });

  async function choose(bookshelfId: string) {
    const { defaultBookshelfId } = state.value;
    state.value = { status: "choosing", bookshelfId, defaultBookshelfId };
    try {
      const answer = await bookshelvesApi.chooseDefault(bookshelfId);
      if (answer.outcome === "chosen")
        state.value = {
          status: "chosen",
          bookshelfId,
          defaultBookshelfId: bookshelfId,
        };
      else
        state.value = {
          status: "unavailable",
          bookshelfId,
          defaultBookshelfId,
        };
    } catch {
      state.value = { status: "unavailable", bookshelfId, defaultBookshelfId };
    }
  }

  return { state, choose };
}
