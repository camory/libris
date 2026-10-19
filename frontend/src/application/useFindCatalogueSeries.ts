import { ref, type Ref } from "vue";
import type { SeriesApi } from "./SeriesApi";

export type FoundSeriesState = { status: "loading" } | { status: "notFound" };

export function useFindCatalogueSeries(seriesApi: SeriesApi): {
  state: Ref<FoundSeriesState>;
  find: (id: string) => Promise<void>;
} {
  const state = ref<FoundSeriesState>({ status: "loading" });

  async function find(id: string) {
    state.value = { status: "loading" };
    await seriesApi.find(id);
    state.value = { status: "notFound" };
  }

  return { state, find };
}
