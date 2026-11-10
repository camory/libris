import type {
  EditionDetails,
  EditionsApi,
  SaveAnswer,
} from "../../application/EditionsApi";
import { bookOf, type BookResponse } from "./bookOf";
import { problemOf } from "./problemOf";

const problems = new Map([
  [400, "/problems/validation"],
  [404, "/problems/not-found"],
]);

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
          "X-Requested-With": "XMLHttpRequest",
        },
        body: JSON.stringify(edition),
      },
    );
    if (response.status !== 200) return problemOf(response, problems);
    const body = (await response.json()) as BookResponse;
    return { outcome: "saved", book: bookOf(body) };
  }
}
