import type { Bookshelf } from "./Bookshelf";

export type MembershipRole = "OWNER" | "VIEWER";

export interface Membership {
  bookshelf: Bookshelf;
  role: MembershipRole;
}
