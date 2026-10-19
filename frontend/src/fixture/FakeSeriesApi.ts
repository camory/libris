import type { FindSeriesAnswer, SeriesApi } from "../application/SeriesApi";
import type { Series } from "../domain/Series";

const notFound = { outcome: "problem", type: "/problems/not-found" } as const;

export class FakeSeriesApi implements SeriesApi {
  constructor(
    private readonly series: Series[] | Error,
    private readonly found: FindSeriesAnswer | Error = new Error(
      "no find in this scenario",
    ),
  ) {}

  owned(): Promise<Series[]> {
    return this.series instanceof Error
      ? Promise.reject(this.series)
      : Promise.resolve([...this.series]);
  }

  find(id: string): Promise<FindSeriesAnswer> {
    if (this.found instanceof Error) return Promise.reject(this.found);
    if (this.found.outcome === "problem") return Promise.resolve(this.found);
    if (this.found.series.id !== id) return Promise.resolve(notFound);
    return Promise.resolve({
      outcome: "found",
      series: { ...this.found.series, books: [...this.found.series.books] },
    });
  }
}
