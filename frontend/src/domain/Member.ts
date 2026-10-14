import type { KnownReader } from "./KnownReader";
import type { MembershipRole } from "./Membership";
import { nameOrder } from "./nameOrder";

export interface Member {
  reader: KnownReader;
  role: MembershipRole;
}

export function membersInOrder(
  members: Member[],
  readerId: string,
  locale: string,
): Member[] {
  const byName = nameOrder(locale);
  const others = members.filter((member) => member.reader.id !== readerId);
  const withRole = (role: MembershipRole) =>
    others
      .filter((member) => member.role === role)
      .sort((a, b) => byName(a.reader.displayName, b.reader.displayName));
  return [
    ...members.filter((member) => member.reader.id === readerId),
    ...withRole("OWNER"),
    ...withRole("VIEWER"),
  ];
}

export function nonMembersInOrder(
  readers: KnownReader[],
  members: Member[],
  locale: string,
): KnownReader[] {
  const memberIds = new Set(members.map((member) => member.reader.id));
  const byName = nameOrder(locale);
  return readers
    .filter((reader) => !memberIds.has(reader.id))
    .sort((a, b) => byName(a.displayName, b.displayName));
}
