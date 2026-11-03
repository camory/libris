import { ref, type Ref } from "vue";
import type { SeriesApi } from "./SeriesApi";

export function useListSeriesNames(seriesApi: SeriesApi): {
  names: Ref<string[]>;
  list: () => Promise<void>;
} {
  const names = ref<string[]>([]);

  async function list() {
    names.value = await seriesApi.names();
  }

  return { names, list };
}
