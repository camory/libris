import type { Bookshelf } from "./Bookshelf";

export interface Copy {
  id: string;
  bookshelf: Bookshelf;
}

export function bookshelvesHolding(
  copies: Copy[],
  locale: string,
): { bookshelf: Bookshelf; count: number }[] {
  const held = new Map<string, { bookshelf: Bookshelf; count: number }>();
  for (const { bookshelf } of copies) {
    const holding = held.get(bookshelf.id) ?? { bookshelf, count: 0 };
    holding.count += 1;
    held.set(bookshelf.id, holding);
  }
  return [...held.values()];
}
