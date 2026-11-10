import { ref, type Ref } from "vue";
import type { Book } from "../domain/Book";
import type { SourceEdition } from "../domain/SourceEdition";
import type { EditionDetails, EditionsApi } from "./EditionsApi";

export type SaveState =
  { status: "ready" } | { status: "saving" } | { status: "saved"; book: Book };

export function useSaveEdition(editionsApi: EditionsApi): {
  state: Ref<SaveState>;
  save: (id: string, edition: SourceEdition) => Promise<void>;
} {
  const state = ref<SaveState>({ status: "ready" });

  async function save(id: string, edition: SourceEdition) {
    state.value = { status: "saving" };
    const answer = await editionsApi.save(id, detailsOf(edition));
    if (answer.outcome === "saved")
      state.value = { status: "saved", book: answer.book };
  }

  return { state, save };
}

function detailsOf(edition: SourceEdition): EditionDetails {
  const details = { ...edition };
  Reflect.deleteProperty(details, "isbn13");
  return details;
}
