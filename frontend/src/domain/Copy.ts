import type { Bookshelf } from "./Bookshelf";
import type { MembershipRole } from "./Membership";
import { nameOrder } from "./nameOrder";

export interface Copy {
  id: string;
  bookshelf: ReaderBookshelf;
}

export interface ReaderBookshelf extends Bookshelf {
  role?: MembershipRole;
}

export interface Holding {
  bookshelf: ReaderBookshelf;
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
