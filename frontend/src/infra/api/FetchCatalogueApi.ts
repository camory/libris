import type { BookPage, CatalogueApi } from "../../application/CatalogueApi";
import type { AuthorRole, Kind } from "../../domain/SourceEdition";

interface BookResponse {
  id: string;
  isbn13: string | null;
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
  coverUrl: string | null;
  copies: { id: string; bookshelf: { id: string; name: string } }[];
}

interface BookPageResponse {
  books: BookResponse[];
  next: string | null;
}

export class FetchCatalogueApi implements CatalogueApi {
  constructor(private readonly baseUrl: string) {}

  async browse(after: string | null): Promise<BookPage> {
    const query =
      after === null ? "" : `?${new URLSearchParams({ after }).toString()}`;
    const response = await fetch(`${this.baseUrl}/api/v1/books${query}`, {
      headers: { Accept: "application/json, application/problem+json" },
    });
    if (response.status !== 200) {
      throw Error(`The catalogue answered ${response.status}`);
    }
    const body = (await response.json()) as BookPageResponse;
    return {
      books: body.books.map((book) => ({
        id: book.id,
        isbn13: book.isbn13,
        kind: book.kind,
        title: book.title,
        subtitle: book.subtitle,
        authors: book.authors.map((author) => ({
          name: author.name,
          role: author.role,
        })),
        series: book.series,
        collection: book.collection,
        publisher: book.publisher,
        publicationYear: book.publicationYear,
        language: book.language,
        pageCount: book.pageCount,
        summary: book.summary,
        coverUrl: book.coverUrl,
        copies: book.copies.map((copy) => ({
          id: copy.id,
          bookshelf: { id: copy.bookshelf.id, name: copy.bookshelf.name },
        })),
      })),
      next: body.next,
    };
  }
}
