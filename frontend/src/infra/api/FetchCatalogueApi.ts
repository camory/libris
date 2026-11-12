import type {
  BookPage,
  CatalogueApi,
  FindBookAnswer,
} from "../../application/CatalogueApi";
import type { SearchApi, SearchPage } from "../../application/SearchApi";
import { bookOf, type BookResponse } from "./bookOf";
import { problemOf, validationOrNotFound } from "./problemOf";

interface BookPageResponse {
  books: BookResponse[];
  next: string | null;
}

export class FetchCatalogueApi implements CatalogueApi, SearchApi {
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

  async search(query: string, after: string | null): Promise<SearchPage> {
    const parameters = new URLSearchParams({ q: query });
    if (after !== null) parameters.set("after", after);
    const response = await fetch(
      `${this.baseUrl}/api/v1/books?${parameters.toString()}`,
      {
        headers: { Accept: "application/json, application/problem+json" },
      },
    );
    if (response.status !== 200) {
      throw Error(`The search answered ${response.status}`);
    }
    return { books: [], next: null, near: false };
  }

  async find(id: string): Promise<FindBookAnswer> {
    const response = await fetch(
      `${this.baseUrl}/api/v1/books/${encodeURIComponent(id)}`,
      {
        headers: { Accept: "application/json, application/problem+json" },
      },
    );
    if (response.status !== 200)
      return problemOf(response, validationOrNotFound);
    const body = (await response.json()) as BookResponse;
    return { outcome: "found", book: bookOf(body) };
  }
}
