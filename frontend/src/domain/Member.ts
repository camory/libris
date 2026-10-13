import type { KnownReader } from "./KnownReader";
import type { MembershipRole } from "./Membership";
import { nameOrder } from "./nameOrder";

export interface Member {
  reader: KnownReader;
  role: MembershipRole;
}

export function membersInOrder(members: Member[], locale: string): Member[] {
  const byName = nameOrder(locale);
  const withRole = (role: MembershipRole) =>
    members
      .filter((member) => member.role === role)
      .sort((a, b) => byName(a.reader.displayName, b.reader.displayName));
  return [...withRole("OWNER"), ...withRole("VIEWER")];
}
