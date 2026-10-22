import { ref, type Ref } from "vue";
import type { Book } from "../domain/Book";
import type { CatalogueApi, FindBookAnswer } from "./CatalogueApi";

export type FoundEditionState =
  | { status: "loading" }
  | { status: "found"; book: Book }
  | { status: "notFound" }
  | { status: "unavailable" };

export function useFindCatalogueEdition(catalogueApi: CatalogueApi): {
  state: Ref<FoundEditionState>;
  find: (id: string) => Promise<void>;
} {
  const state = ref<FoundEditionState>({ status: "loading" });

  async function find(id: string) {
    state.value = { status: "loading" };
    try {
      state.value = stateOf(await catalogueApi.find(id));
    } catch {
      state.value = { status: "unavailable" };
    }
  }

  return { state, find };
}

function stateOf(answer: FindBookAnswer): FoundEditionState {
  if (answer.outcome === "found") return { status: "found", book: answer.book };
  switch (answer.type) {
    case "/problems/not-found":
    case "/problems/validation":
      return { status: "notFound" };
    default:
      return { status: "unavailable" };
  }
}
