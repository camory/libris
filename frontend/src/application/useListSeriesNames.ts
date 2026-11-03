import { ref, type Ref } from "vue";
import type { SeriesApi } from "./SeriesApi";

export function useListSeriesNames(seriesApi: SeriesApi): {
  names: Ref<string[] | null>;
  list: () => Promise<void>;
} {
  const names = ref<string[] | null>(null);

  async function list() {
    try {
      names.value = await seriesApi.names();
    } catch {
      names.value = null;
    }
  }

  return { names, list };
}
