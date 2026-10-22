import type { BookPage, CatalogueApi } from "../../application/CatalogueApi";
import type { FindBookAnswer } from "../../application/EditionPageApi";
import { bookOf, type BookResponse } from "./bookOf";
import { problemOf } from "./problemOf";

interface BookPageResponse {
  books: BookResponse[];
  next: string | null;
}

const problems = new Map([[404, "/problems/not-found"]]);

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
    return { books: body.books.map(bookOf), next: body.next };
  }

  async find(id: string): Promise<FindBookAnswer> {
    const response = await fetch(`${this.baseUrl}/api/v1/books/${id}`, {
      headers: { Accept: "application/json, application/problem+json" },
    });
    if (response.status !== 200) return problemOf(response, problems);
    const body = (await response.json()) as BookResponse;
    return { outcome: "found", book: bookOf(body) };
  }
}
