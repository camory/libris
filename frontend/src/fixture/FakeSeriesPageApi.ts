import type {
  FindSeriesAnswer,
  SeriesPageApi,
} from "../application/SeriesPageApi";

const notFound = { outcome: "problem", type: "/problems/not-found" } as const;

export class FakeSeriesPageApi implements SeriesPageApi {
  constructor(private readonly answer: FindSeriesAnswer | Error) {}

  find(id: string): Promise<FindSeriesAnswer> {
    if (this.answer instanceof Error) return Promise.reject(this.answer);
    if (this.answer.outcome === "problem") return Promise.resolve(this.answer);
    if (this.answer.series.id !== id) return Promise.resolve(notFound);
    return Promise.resolve({
      outcome: "found",
      series: { ...this.answer.series, books: [...this.answer.series.books] },
    });
  }
}
