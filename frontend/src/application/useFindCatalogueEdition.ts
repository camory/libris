import { ref, type Ref } from "vue";
import type { Book } from "../domain/Book";
import type { CatalogueApi, FindBookAnswer } from "./CatalogueApi";
import { findFailureOf, type FindFailure } from "./FindFailure";

export type FoundEditionState =
  { status: "loading" } | { status: "found"; book: Book } | FindFailure;

export function useFindCatalogueEdition(catalogueApi: CatalogueApi): {
  state: Ref<FoundEditionState>;
  find: (id: string) => Promise<void>;
  refresh: (id: string) => Promise<void>;
  show: (book: Book) => void;
} {
  const state = ref<FoundEditionState>({ status: "loading" });

  async function find(id: string) {
    state.value = { status: "loading" };
    await refresh(id);
  }

  async function refresh(id: string) {
    try {
      state.value = stateOf(await catalogueApi.find(id));
    } catch {
      state.value = { status: "unavailable" };
    }
  }

  function show(book: Book) {
    state.value = { status: "found", book };
  }

  return { state, find, refresh, show };
}

function stateOf(answer: FindBookAnswer): FoundEditionState {
  if (answer.outcome === "found") return { status: "found", book: answer.book };
  return findFailureOf(answer.type);
}
