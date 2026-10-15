import type { InjectionKey } from "vue";
import type { Series } from "../domain/Series";

export interface SeriesApi {
  owned(): Promise<Series[]>;
}

export const seriesApiKey: InjectionKey<SeriesApi> = Symbol("SeriesApi");
