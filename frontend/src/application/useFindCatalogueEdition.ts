import { ref, type Ref } from "vue";
import type { Book } from "../domain/Book";
import type { CatalogueApi } from "./CatalogueApi";

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
      const answer = await catalogueApi.find(id);
      state.value =
        answer.outcome === "found"
          ? { status: "found", book: answer.book }
          : { status: "notFound" };
    } catch {
      state.value = { status: "unavailable" };
    }
  }

  return { state, find };
}
