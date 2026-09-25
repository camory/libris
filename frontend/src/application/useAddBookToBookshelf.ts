import { ref, type Ref } from "vue";
import type { Copy } from "../domain/Copy";
import type { BookshelfApi } from "./BookshelfApi";
import type { MeApi } from "./MeApi";

export type AddState =
  | { status: "ready" }
  | { status: "adding" }
  | { status: "added"; copy: Copy }
  | { status: "notAdded" };

export function useAddBookToBookshelf(
  _meApi: MeApi,
  _bookshelfApi: BookshelfApi,
): { state: Ref<AddState> } {
  const state = ref<AddState>({ status: "ready" });
  return { state };
}
