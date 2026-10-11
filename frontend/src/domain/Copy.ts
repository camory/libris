import type { Bookshelf } from "./Bookshelf";
import { nameOrder } from "./nameOrder";

export interface Copy {
  id: string;
  bookshelf: Bookshelf;
}

export interface Holding {
  bookshelf: Bookshelf;
  count: number;
}

export function bookshelvesHolding(
  copies: Copy[],
  locale: string,
): Holding[] {
  const held = new Map<string, Holding>();
  for (const { bookshelf } of copies) {
    const holding = held.get(bookshelf.id) ?? { bookshelf, count: 0 };
    holding.count += 1;
    held.set(bookshelf.id, holding);
  }
  const byName = nameOrder(locale);
  return [...held.values()].sort((a, b) =>
    byName(a.bookshelf.name, b.bookshelf.name),
  );
}
