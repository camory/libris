import type { CopiesApi, RemoveAnswer } from "../application/CopiesApi";

export class FakeCopiesApi implements CopiesApi {
  readonly asked: string[] = [];

  constructor(private readonly answer: RemoveAnswer | Error) {}

  remove(copyId: string): Promise<RemoveAnswer> {
    this.asked.push(copyId);
    return this.answer instanceof Error
      ? Promise.reject(this.answer)
      : Promise.resolve(this.answer);
  }
}
