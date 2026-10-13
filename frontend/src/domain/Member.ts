import type { KnownReader } from "./KnownReader";
import type { MembershipRole } from "./Membership";

export interface Member {
  reader: KnownReader;
  role: MembershipRole;
}

export function membersInOrder(members: Member[], locale: string): Member[] {
  void locale;
  return [
    ...members.filter(({ role }) => role === "OWNER"),
    ...members.filter(({ role }) => role === "VIEWER"),
  ];
}
