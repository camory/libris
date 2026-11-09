import type { AuthorsApi } from "../../application/AuthorsApi";

export class FetchAuthorsApi implements AuthorsApi {
  constructor(private readonly baseUrl: string) {}

  async names(): Promise<string[]> {
    const response = await fetch(`${this.baseUrl}/api/v1/author-names`, {
      headers: { Accept: "application/json, application/problem+json" },
    });
    if (response.status !== 200) {
      throw Error(`The author names answered ${response.status}`);
    }
    return (await response.json()) as string[];
  }
}
