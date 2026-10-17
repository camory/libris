import type { Book } from "./Book";
import { nameOrder } from "./nameOrder";
import type { Kind } from "./SourceEdition";

export interface Series {
  id?: string;
  name: string;
  volumeCount: number;
  coverUrl: string | null;
  kind: Kind | null;
}

export interface SeriesWithBooks extends Series {
  id: string;
  books: Book[];
}

export function seriesInOrder(series: Series[], locale: string): Series[] {
  const byName = nameOrder(locale);
  return [...series].sort((a, b) => byName(a.name, b.name));
}
