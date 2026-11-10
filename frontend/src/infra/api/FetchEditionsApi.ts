import type {
  EditionDetails,
  EditionsApi,
  SaveAnswer,
} from "../../application/EditionsApi";
import { bookOf, type BookResponse } from "./bookOf";

export class FetchEditionsApi implements EditionsApi {
  constructor(private readonly baseUrl: string) {}

  async save(id: string, edition: EditionDetails): Promise<SaveAnswer> {
    const response = await fetch(
      `${this.baseUrl}/api/v1/books/${encodeURIComponent(id)}`,
      {
        method: "PUT",
        headers: {
          Accept: "application/json, application/problem+json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify(edition),
      },
    );
    const body = (await response.json()) as BookResponse;
    return { outcome: "saved", book: bookOf(body) };
  }
}
