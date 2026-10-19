import type { BookPage, CatalogueApi } from "../../application/CatalogueApi";
import { bookOf, type BookResponse } from "./bookOf";

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
    return { books: body.books.map(bookOf), next: body.next };
  }
}
