import type { SeriesApi } from "../application/SeriesApi";
import type { Series } from "../domain/Series";

export class FakeSeriesApi implements SeriesApi {
  constructor(private readonly series: Series[] | Error) {}

  owned(): Promise<Series[]> {
    return this.series instanceof Error
      ? Promise.reject(this.series)
      : Promise.resolve([...this.series]);
  }
}
