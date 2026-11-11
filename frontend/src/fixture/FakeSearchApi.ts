import type { SearchApi, SearchPage } from "../application/SearchApi";

type SearchAnswer = SearchPage | Error | Promise<SearchPage>;

export class FakeSearchApi implements SearchApi {
  readonly asked: { query: string; after: string | null }[] = [];
  private readonly answers: Map<string, SearchAnswer[]>;

  constructor(answers: Record<string, SearchAnswer[]> = {}) {
    this.answers = new Map(
      Object.entries(answers).map(([query, pages]) => [query, [...pages]]),
    );
  }

  search(query: string, after: string | null): Promise<SearchPage> {
    this.asked.push({ query, after });
    const answer = this.answers.get(query)?.shift();
    if (answer === undefined)
      return Promise.reject(Error(`No answer left to ${query}`));
    return answer instanceof Error
      ? Promise.reject(answer)
      : Promise.resolve(answer);
  }
}
