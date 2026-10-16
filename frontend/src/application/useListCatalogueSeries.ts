import { ref, type Ref } from "vue";
import type { SeriesApi } from "./SeriesApi";

export type CatalogueSeriesState = { status: "loading" } | { status: "empty" };

export function useListCatalogueSeries(seriesApi: SeriesApi): {
  state: Ref<CatalogueSeriesState>;
  list: () => Promise<void>;
} {
  const state = ref<CatalogueSeriesState>({ status: "loading" });

  async function list() {
    await seriesApi.owned();
    state.value = { status: "empty" };
  }

  return { state, list };
}
