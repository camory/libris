import type { MeApi } from "../application/MeApi";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Reader } from "../domain/Reader";

export class FakeMeApi implements MeApi {
  constructor(private reader: Reader) {}

  currentReader(): Promise<Reader> {
    return Promise.resolve(this.reader);
  }

  defaultIs(defaultBookshelf: Bookshelf) {
    this.reader = { ...this.reader, defaultBookshelf };
  }
}
