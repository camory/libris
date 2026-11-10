import type { CopiesApi, RemoveAnswer } from "../../application/CopiesApi";
import { problemOf, validationOrNotFound } from "./problemOf";

export class FetchCopiesApi implements CopiesApi {
  constructor(private readonly baseUrl: string) {}

  async remove(copyId: string): Promise<RemoveAnswer> {
    const response = await fetch(`${this.baseUrl}/api/v1/copies/${copyId}`, {
      method: "DELETE",
      headers: {
        Accept: "application/json, application/problem+json",
        "X-Requested-With": "XMLHttpRequest",
      },
    });
    if (response.status !== 204)
      return problemOf(response, validationOrNotFound);
    return { outcome: "removed" };
  }
}
