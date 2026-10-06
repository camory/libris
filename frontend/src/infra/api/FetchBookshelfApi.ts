import type { AddAnswer, BookshelfApi } from "../../application/BookshelfApi";
import type { SourceEdition } from "../../domain/SourceEdition";
import { problemOf } from "./problemOf";

interface CopyResponse {
  id: string;
  bookshelf: { id: string; name: string };
}

const problems = new Map([
  [400, "/problems/validation"],
  [404, "/problems/not-found"],
]);

export class FetchBookshelfApi implements BookshelfApi {
  constructor(private readonly baseUrl: string) {}

  async add(
    bookshelfId: string,
    edition: SourceEdition,
    coverSource?: string | null,
  ): Promise<AddAnswer> {
    const response = await fetch(
      `${this.baseUrl}/api/v1/bookshelves/${bookshelfId}/books`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json, application/problem+json",
          "X-Requested-With": "XMLHttpRequest",
        },
        body: JSON.stringify({
          isbn13: edition.isbn13,
          kind: edition.kind,
          title: edition.title,
          subtitle: edition.subtitle,
          authors: edition.authors,
          series: edition.series,
          collection: edition.collection,
          publisher: edition.publisher,
          publicationYear: edition.publicationYear,
          language: edition.language,
          pageCount: edition.pageCount,
          summary: edition.summary,
          coverSource: coverSource ?? null,
        }),
      },
    );
    if (response.status === 201) {
      const body = (await response.json()) as CopyResponse;
      return {
        outcome: "added",
        copy: {
          id: body.id,
          bookshelf: { id: body.bookshelf.id, name: body.bookshelf.name },
        },
      };
    }
    return problemOf(response, problems);
  }
}
