import type { SeriesApi } from "../../application/SeriesApi";
import type { Series } from "../../domain/Series";
import type { Kind } from "../../domain/SourceEdition";

interface SeriesResponse {
  id: string;
  name: string;
  volumeCount: number;
  coverUrl: string | null;
  kind: Kind | null;
}

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
    return body.map((series) => ({
      id: series.id,
      name: series.name,
      volumeCount: series.volumeCount,
      coverUrl: series.coverUrl,
      kind: series.kind,
    }));
  }
}
