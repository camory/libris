import type { Reader } from "../domain/Reader";

export interface MeApi {
  currentReader(): Promise<Reader>;
}
