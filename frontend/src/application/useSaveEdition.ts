import { ref, type Ref } from "vue";
import type { Book } from "../domain/Book";
import type { SourceEdition } from "../domain/SourceEdition";
import type { EditionDetails, EditionsApi } from "./EditionsApi";

export type SaveState =
  | { status: "ready" }
  | { status: "saving" }
  | { status: "saved"; book: Book }
  | { status: "notSaved" };

export function useSaveEdition(editionsApi: EditionsApi): {
  state: Ref<SaveState>;
  save: (id: string, edition: SourceEdition) => Promise<void>;
} {
  const state = ref<SaveState>({ status: "ready" });

  async function save(id: string, edition: SourceEdition) {
    state.value = { status: "saving" };
    try {
      const answer = await editionsApi.save(id, detailsOf(edition));
      state.value =
        answer.outcome === "saved"
          ? { status: "saved", book: answer.book }
          : { status: "notSaved" };
    } catch {
      state.value = { status: "notSaved" };
    }
  }

  return { state, save };
}

function detailsOf(edition: SourceEdition): EditionDetails {
  const details = { ...edition };
  Reflect.deleteProperty(details, "isbn13");
  return details;
}
