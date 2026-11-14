import type { IsbnAnswer, IsbnApi } from "../application/IsbnApi";

export class FakeIsbnApi implements IsbnApi {
  readonly asked: string[] = [];

  constructor(
    private readonly answer: IsbnAnswer | Promise<IsbnAnswer> | Error,
  ) {}

  lookUp(isbn13: string): Promise<IsbnAnswer> {
    this.asked.push(isbn13);
    return this.answer instanceof Error
      ? Promise.reject(this.answer)
      : Promise.resolve(this.answer);
  }
}
