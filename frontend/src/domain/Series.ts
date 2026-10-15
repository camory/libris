import type { Kind } from "./SourceEdition";

export interface Series {
  name: string;
  volumeCount: number;
  coverUrl: string | null;
  kind: Kind | null;
}
