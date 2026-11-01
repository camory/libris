import type { InjectionKey } from "vue";
import type { Series, SeriesWithBooks } from "../domain/Series";

export type FindSeriesAnswer =
  | { outcome: "found"; series: SeriesWithBooks }
  | { outcome: "problem"; type: string };

export interface SeriesApi {
  owned(): Promise<Series[]>;
  find(id: string): Promise<FindSeriesAnswer>;
  names(): Promise<string[]>;
}

export const seriesApiKey: InjectionKey<SeriesApi> = Symbol("SeriesApi");
