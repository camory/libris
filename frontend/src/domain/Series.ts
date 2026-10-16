import { nameOrder } from "./nameOrder";
import type { Kind } from "./SourceEdition";

export interface Series {
  name: string;
  volumeCount: number;
  coverUrl: string | null;
  kind: Kind | null;
}

export function seriesInOrder(series: Series[], locale: string): Series[] {
  const byName = nameOrder(locale);
  return [...series].sort((a, b) => byName(a.name, b.name));
}
