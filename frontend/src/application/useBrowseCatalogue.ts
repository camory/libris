import { ref, type Ref } from "vue";
import type { CatalogueApi } from "./CatalogueApi";

export type CatalogueState = { status: "loading" };

export function useBrowseCatalogue(catalogueApi: CatalogueApi): {
  state: Ref<CatalogueState>;
  browse: () => Promise<void>;
} {
  const state = ref<CatalogueState>({ status: "loading" });

  async function browse() {}

  return { state, browse };
}
