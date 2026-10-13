import type { AddAnswer, BookshelfApi } from "../application/BookshelfApi";
import type { SourceEdition } from "../domain/SourceEdition";

export class FakeBookshelfApi implements BookshelfApi {
  readonly asked: {
    bookshelfId: string;
    edition: SourceEdition;
    coverSource?: string | null;
  }[] = [];

  constructor(
    private readonly answer:
      | AddAnswer
      | Promise<AddAnswer>
      | Error
      | ((bookshelfId: string) => AddAnswer),
  ) {}

  add(
    bookshelfId: string,
    edition: SourceEdition,
    coverSource?: string | null,
  ): Promise<AddAnswer> {
    this.asked.push(
      coverSource === undefined
        ? { bookshelfId, edition }
        : { bookshelfId, edition, coverSource },
    );
    const answer =
      typeof this.answer === "function"
        ? this.answer(bookshelfId)
        : this.answer;
    return answer instanceof Error
      ? Promise.reject(answer)
      : Promise.resolve(answer);
  }
}
