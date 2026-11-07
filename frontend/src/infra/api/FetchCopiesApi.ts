import type { CopiesApi, RemoveAnswer } from "../../application/CopiesApi";
import { problemOf } from "./problemOf";

const problems = new Map([[404, "/problems/not-found"]]);

export class FetchCopiesApi implements CopiesApi {
  constructor(private readonly baseUrl: string) {}

  async remove(copyId: string): Promise<RemoveAnswer> {
    const response = await fetch(`${this.baseUrl}/api/v1/copies/${copyId}`, {
      method: "DELETE",
      headers: { Accept: "application/json, application/problem+json" },
    });
    if (response.status !== 204) return problemOf(response, problems);
    return { outcome: "removed" };
  }
}
