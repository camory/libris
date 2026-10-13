import type { Bookshelf } from "./Bookshelf";

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
  return [
    ...memberships.filter(isDefault),
    ...memberships.filter((membership) => !isDefault(membership)),
  ];
}
