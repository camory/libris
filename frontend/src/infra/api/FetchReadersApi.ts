import type { ReadersApi } from "../../application/ReadersApi";
import type { KnownReader } from "../../domain/KnownReader";

interface ReaderResponse {
  id: string;
  displayName: string;
}

export class FetchReadersApi implements ReadersApi {
  constructor(private readonly baseUrl: string) {}

  async known(): Promise<KnownReader[]> {
    const response = await fetch(`${this.baseUrl}/api/v1/readers`, {
      headers: { Accept: "application/json, application/problem+json" },
    });
    const body = (await response.json()) as ReaderResponse[];
    return body.map((reader) => ({
      id: reader.id,
      displayName: reader.displayName,
    }));
  }
}
