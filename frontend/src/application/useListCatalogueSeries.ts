import { ref, type Ref } from "vue";
import type { Series } from "../domain/Series";
import type { SeriesApi } from "./SeriesApi";

export type CatalogueSeriesState =
  | { status: "loading" }
  | { status: "empty" }
  | { status: "listed"; series: Series[] };

export function useListCatalogueSeries(seriesApi: SeriesApi): {
  state: Ref<CatalogueSeriesState>;
  list: () => Promise<void>;
} {
  const state = ref<CatalogueSeriesState>({ status: "loading" });

  async function list() {
    const series = await seriesApi.owned();
    state.value =
      series.length === 0 ? { status: "empty" } : { status: "listed", series };
  }

  return { state, list };
}
