import type { IsbnApi, IsbnAnswer } from "../../application/IsbnApi";
import type {
  AuthorRole,
  Source,
  SourceEdition,
} from "../../domain/SourceEdition";

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
  sources: Source[];
}

export class FetchIsbnApi implements IsbnApi {
  constructor(private readonly baseUrl: string) {}

  async lookUp(isbn13: string): Promise<IsbnAnswer> {
    const response = await fetch(`${this.baseUrl}/api/v1/isbn/${isbn13}`, {
      headers: { Accept: "application/json, application/problem+json" },
    });
    const body = (await response.json()) as IsbnResponse;
    return { outcome: "found", edition: editionOf(body) };
  }
}

function editionOf(body: IsbnResponse): SourceEdition {
  return {
    isbn13: body.isbn13,
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
    coverUrl: body.coverUrl,
    sources: body.sources,
  };
}
