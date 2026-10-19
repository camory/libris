import type { SeriesApi } from "../../application/SeriesApi";
import type { FindSeriesAnswer } from "../../application/SeriesPageApi";
import type { Series } from "../../domain/Series";
import type { Kind } from "../../domain/SourceEdition";
import { bookOf, type BookResponse } from "./bookOf";
import { problemOf } from "./problemOf";

interface SeriesResponse {
  id: string;
  name: string;
  volumeCount: number;
  coverUrl: string | null;
  kind: Kind | null;
}

interface SeriesWithBooksResponse extends SeriesResponse {
  books: BookResponse[];
}

const problems = new Map([[404, "/problems/not-found"]]);

export class FetchSeriesApi implements SeriesApi {
  constructor(private readonly baseUrl: string) {}

  async owned(): Promise<Series[]> {
    const response = await fetch(`${this.baseUrl}/api/v1/series`, {
      headers: { Accept: "application/json, application/problem+json" },
    });
    if (response.status !== 200) {
      throw Error(`The series answered ${response.status}`);
    }
    const body = (await response.json()) as SeriesResponse[];
    return body.map(seriesOf);
  }

  async find(id: string): Promise<FindSeriesAnswer> {
    const response = await fetch(`${this.baseUrl}/api/v1/series/${id}`, {
      headers: { Accept: "application/json, application/problem+json" },
    });
    if (response.status !== 200) return problemOf(response, problems);
    const body = (await response.json()) as SeriesWithBooksResponse;
    return {
      outcome: "found",
      series: { ...seriesOf(body), books: body.books.map(bookOf) },
    };
  }
}

function seriesOf(series: SeriesResponse): Series {
  return {
    id: series.id,
    name: series.name,
    volumeCount: series.volumeCount,
    coverUrl: series.coverUrl,
    kind: series.kind,
  };
}
