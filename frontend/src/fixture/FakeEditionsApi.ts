import type {
  EditionDetails,
  EditionsApi,
  SaveAnswer,
} from "../application/EditionsApi";

export class FakeEditionsApi implements EditionsApi {
  readonly asked: { id: string; edition: EditionDetails }[] = [];

  constructor(private readonly answer: SaveAnswer | Error) {}

  save(id: string, edition: EditionDetails): Promise<SaveAnswer> {
    this.asked.push({ id, edition });
    return this.answer instanceof Error
      ? Promise.reject(this.answer)
      : Promise.resolve(this.answer);
  }
}
