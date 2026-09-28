import { ref, type Ref } from "vue";
import type { CatalogueApi } from "./CatalogueApi";

export type CatalogueState = { status: "loading" } | { status: "empty" };

export function useBrowseCatalogue(catalogueApi: CatalogueApi): {
  state: Ref<CatalogueState>;
  browse: () => Promise<void>;
} {
  const state = ref<CatalogueState>({ status: "loading" });

  async function browse() {
    const page = await catalogueApi.browse(null);
    if (page.books.length === 0) {
      state.value = { status: "empty" };
    }
  }

  return { state, browse };
}
