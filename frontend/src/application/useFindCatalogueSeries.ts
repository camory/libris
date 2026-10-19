import { ref, type Ref } from "vue";
import type { SeriesWithBooks } from "../domain/Series";
import type { SeriesApi } from "./SeriesApi";

export type FoundSeriesState =
  | { status: "loading" }
  | { status: "found"; series: SeriesWithBooks }
  | { status: "notFound" };

export function useFindCatalogueSeries(seriesApi: SeriesApi): {
  state: Ref<FoundSeriesState>;
  find: (id: string) => Promise<void>;
} {
  const state = ref<FoundSeriesState>({ status: "loading" });

  async function find(id: string) {
    state.value = { status: "loading" };
    const answer = await seriesApi.find(id);
    state.value =
      answer.outcome === "found"
        ? { status: "found", series: answer.series }
        : { status: "notFound" };
  }

  return { state, find };
}
