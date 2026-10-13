import type { Bookshelf } from "./Bookshelf";
import { nameOrder } from "./nameOrder";

export type MembershipRole = "OWNER" | "VIEWER";

export interface Membership {
  bookshelf: Bookshelf;
  role: MembershipRole;
}

export function membershipsInOrder(
  memberships: Membership[],
  defaultBookshelfId: string,
  locale: string,
): Membership[] {
  const isDefault = ({ bookshelf }: Membership) =>
    bookshelf.id === defaultBookshelfId;
  const byName = nameOrder(locale);
  return [
    ...memberships.filter(isDefault),
    ...memberships
      .filter((membership) => !isDefault(membership))
      .sort((a, b) => byName(a.bookshelf.name, b.bookshelf.name)),
  ];
}
