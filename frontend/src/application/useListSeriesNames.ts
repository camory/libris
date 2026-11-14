import { ref, type Ref } from "vue";
import type { SeriesApi } from "./SeriesApi";

export function useListSeriesNames(seriesApi: SeriesApi): {
  names: Ref<string[] | null>;
  list: () => Promise<void>;
} {
  const names = ref<string[] | null>(null);
  let latest = 0;

  async function list() {
    const asked = ++latest;
    const answer = await seriesApi.names().catch(() => null);
    if (asked !== latest) return;
    names.value = answer;
  }

  return { names, list };
}
