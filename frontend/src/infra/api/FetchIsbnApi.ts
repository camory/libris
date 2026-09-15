import type { IsbnApi, IsbnAnswer } from "../../application/IsbnApi";
import type { AuthorRole } from "../../domain/SourceEdition";

interface IsbnResponse {
  isbn13: string;
  title: string;
  subtitle: string | null;
  authors: { name: string; role: AuthorRole }[];
  series: { name: string; volumeNumber: number | null } | null;
  collection: string | null;
  publisher: string | null;
  publicationYear: number | null;
  language: string | null;
  pageCount: number | null;
  summary: string | null;
  coverUrl: string | null;
}

interface ProblemResponse {
  type: string;
}

export class FetchIsbnApi implements IsbnApi {
  constructor(private readonly baseUrl: string) {}

  async lookUp(isbn13: string): Promise<IsbnAnswer> {
    const response = await fetch(`${this.baseUrl}/api/v1/isbn/${isbn13}`, {
      headers: { Accept: "application/json, application/problem+json" },
    });
    if (response.status === 200) {
      const body = (await response.json()) as IsbnResponse;
      return { outcome: "found", edition: body };
    }
    const problem = (await response.json()) as ProblemResponse;
    return { outcome: "problem", type: problem.type };
  }
}
