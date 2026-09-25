import type { AddAnswer, BookshelfApi } from "../application/BookshelfApi";

export class FakeBookshelfApi implements BookshelfApi {
  constructor(
    private readonly answer: AddAnswer | Promise<AddAnswer> | Error,
  ) {}

  add(): Promise<AddAnswer> {
    return this.answer instanceof Error
      ? Promise.reject(this.answer)
      : Promise.resolve(this.answer);
  }
}
