import type { AddAnswer, BookshelfApi } from "../application/BookshelfApi";
import type { SourceEdition } from "../domain/SourceEdition";

export class FakeBookshelfApi implements BookshelfApi {
  readonly asked: { bookshelfId: string; edition: SourceEdition }[] = [];

  constructor(
    private readonly answer: AddAnswer | Promise<AddAnswer> | Error,
  ) {}

  add(bookshelfId: string, edition: SourceEdition): Promise<AddAnswer> {
    this.asked.push({ bookshelfId, edition });
    return this.answer instanceof Error
      ? Promise.reject(this.answer)
      : Promise.resolve(this.answer);
  }
}
