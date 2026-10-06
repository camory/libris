import type { IsbnApi, IsbnAnswer } from "../../application/IsbnApi";
import type { AuthorRole, Kind } from "../../domain/SourceEdition";
import { problemOf } from "./problemOf";

interface IsbnResponse {
  id: string | null;
  isbn13: string;
  kind: Kind;
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
  copies: { id: string; bookshelf: { id: string; name: string } }[];
  covers: { source: string; url: string }[];
}

const problems = new Map([
  [400, "/problems/validation"],
  [404, "/problems/not-found"],
  [503, "/problems/sources-unavailable"],
]);

export class FetchIsbnApi implements IsbnApi {
  constructor(private readonly baseUrl: string) {}

  async lookUp(isbn13: string): Promise<IsbnAnswer> {
    const response = await fetch(`${this.baseUrl}/api/v1/isbn/${isbn13}`, {
      headers: { Accept: "application/json, application/problem+json" },
    });
    if (response.status === 200) {
      const body = (await response.json()) as IsbnResponse;
      return {
        outcome: "found",
        id: body.id,
        edition: {
          isbn13: body.isbn13,
          kind: body.kind,
          title: body.title,
          subtitle: body.subtitle,
          authors: body.authors,
          series: body.series,
          collection: body.collection,
          publisher: body.publisher,
          publicationYear: body.publicationYear,
          language: body.language,
          pageCount: body.pageCount,
          summary: body.summary,
        },
        copies: body.copies.map((copy) => ({
          id: copy.id,
          bookshelf: { id: copy.bookshelf.id, name: copy.bookshelf.name },
        })),
        covers: body.covers.map((cover) => ({
          source: cover.source,
          url: cover.url,
        })),
      };
    }
    return problemOf(response, problems);
  }
}
