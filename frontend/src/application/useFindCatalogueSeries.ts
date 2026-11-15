import { ref, type Ref } from "vue";
import type { SeriesWithBooks } from "../domain/Series";
import { findFailureOf, type FindFailure } from "./FindFailure";
import type { FindSeriesAnswer, SeriesApi } from "./SeriesApi";

export type FoundSeriesState =
  | { status: "loading" }
  | { status: "found"; series: SeriesWithBooks }
  | FindFailure;

export function useFindCatalogueSeries(seriesApi: SeriesApi): {
  state: Ref<FoundSeriesState>;
  find: (id: string) => Promise<void>;
} {
  const state = ref<FoundSeriesState>({ status: "loading" });

  async function find(id: string) {
    state.value = { status: "loading" };
    try {
      state.value = stateOf(await seriesApi.find(id));
    } catch {
      state.value = { status: "unavailable" };
    }
  }

  return { state, find };
}

function stateOf(answer: FindSeriesAnswer): FoundSeriesState {
  if (answer.outcome === "found")
    return { status: "found", series: answer.series };
  return findFailureOf(answer.type);
}
