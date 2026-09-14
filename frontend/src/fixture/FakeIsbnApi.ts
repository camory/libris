import type { IsbnAnswer, IsbnApi } from "../application/IsbnApi";

export class FakeIsbnApi implements IsbnApi {
  readonly asked: string[] = [];

  constructor(private readonly answer: IsbnAnswer | Promise<IsbnAnswer>) {}

  lookUp(isbn13: string): Promise<IsbnAnswer> {
    this.asked.push(isbn13);
    return Promise.resolve(this.answer);
  }
}
