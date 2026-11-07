import type { CopiesApi, RemoveAnswer } from "../../application/CopiesApi";

export class FetchCopiesApi implements CopiesApi {
  constructor(private readonly baseUrl: string) {}

  async remove(copyId: string): Promise<RemoveAnswer> {
    await fetch(`${this.baseUrl}/api/v1/copies/${copyId}`, {
      method: "DELETE",
      headers: { Accept: "application/json, application/problem+json" },
    });
    return { outcome: "removed" };
  }
}
