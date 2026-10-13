import type { ReadersApi } from "../application/ReadersApi";
import type { KnownReader } from "../domain/KnownReader";

export class FakeReadersApi implements ReadersApi {
  constructor(private readonly readers: KnownReader[] | Error) {}

  known(): Promise<KnownReader[]> {
    return this.readers instanceof Error
      ? Promise.reject(this.readers)
      : Promise.resolve([...this.readers]);
  }
}
