import type {
  EditionPageApi,
  FindBookAnswer,
} from "../application/EditionPageApi";

const notFound = { outcome: "problem", type: "/problems/not-found" } as const;

export class FakeEditionPageApi implements EditionPageApi {
  constructor(private readonly answer: FindBookAnswer | Error) {}

  find(id: string): Promise<FindBookAnswer> {
    if (this.answer instanceof Error) return Promise.reject(this.answer);
    if (this.answer.outcome === "problem") return Promise.resolve(this.answer);
    if (this.answer.book.id !== id) return Promise.resolve(notFound);
    return Promise.resolve({
      outcome: "found",
      book: { ...this.answer.book, copies: [...this.answer.book.copies] },
    });
  }
}
