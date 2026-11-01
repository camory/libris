import type { SeriesNamesApi } from "../application/SeriesNamesApi";

export class FakeSeriesNamesApi implements SeriesNamesApi {
  constructor(private readonly answer: string[] | Error) {}

  names(): Promise<string[]> {
    return this.answer instanceof Error
      ? Promise.reject(this.answer)
      : Promise.resolve([...this.answer]);
  }
}
