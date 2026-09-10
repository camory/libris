import type { MeApi } from "../application/MeApi";
import type { Reader } from "../domain/Reader";

export class FakeMeApi implements MeApi {
  constructor(private readonly reader: Reader) {}

  currentReader(): Promise<Reader> {
    return Promise.resolve(this.reader);
  }
}
