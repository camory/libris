import type { AuthorsApi } from "../application/AuthorsApi";

export class FakeAuthorsApi implements AuthorsApi {
  constructor(private readonly answer: string[] | Error) {}

  names(): Promise<string[]> {
    return this.answer instanceof Error
      ? Promise.reject(this.answer)
      : Promise.resolve([...this.answer]);
  }
}
