import { ref, type Ref } from "vue";
import type { Copy } from "../domain/Copy";
import type { SourceEdition } from "../domain/SourceEdition";
import type { BookshelfApi } from "./BookshelfApi";
import type { MeApi } from "./MeApi";

export type AddState =
  | { status: "ready" }
  | { status: "adding" }
  | { status: "added"; copy: Copy }
  | { status: "notAdded" };

export function useAddBookToBookshelf(
  meApi: MeApi,
  bookshelfApi: BookshelfApi,
): {
  state: Ref<AddState>;
  add: (edition: SourceEdition) => Promise<void>;
} {
  const state = ref<AddState>({ status: "ready" });

  async function add(edition: SourceEdition) {
    state.value = { status: "adding" };
    try {
      const reader = await meApi.currentReader();
      const answer = await bookshelfApi.add(
        reader.defaultBookshelf.id,
        edition,
      );
      state.value =
        answer.outcome === "added"
          ? { status: "added", copy: answer.copy }
          : { status: "notAdded" };
    } catch {
      state.value = { status: "notAdded" };
    }
  }

  return { state, add };
}
