import type { InjectionKey } from "vue";
import type { SeriesWithBooks } from "../domain/Series";

export type FindSeriesAnswer =
  | { outcome: "found"; series: SeriesWithBooks }
  | { outcome: "problem"; type: string };

export interface SeriesPageApi {
  find(id: string): Promise<FindSeriesAnswer>;
}

export const seriesPageApiKey: InjectionKey<SeriesPageApi> =
  Symbol("SeriesPageApi");
