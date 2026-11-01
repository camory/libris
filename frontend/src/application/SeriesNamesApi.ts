import type { InjectionKey } from "vue";

export interface SeriesNamesApi {
  names(): Promise<string[]>;
}

export const seriesNamesApiKey: InjectionKey<SeriesNamesApi> =
  Symbol("SeriesNamesApi");
