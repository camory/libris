import { ref, type Ref } from "vue";
import type { CatalogueApi } from "./CatalogueApi";

export type FoundEditionState = { status: "loading" } | { status: "notFound" };

export function useFindCatalogueEdition(catalogueApi: CatalogueApi): {
  state: Ref<FoundEditionState>;
  find: (id: string) => Promise<void>;
} {
  const state = ref<FoundEditionState>({ status: "loading" });

  async function find(id: string) {
    state.value = { status: "loading" };
    await catalogueApi.find(id);
    state.value = { status: "notFound" };
  }

  return { state, find };
}
