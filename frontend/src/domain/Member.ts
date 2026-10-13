import type { KnownReader } from "./KnownReader";
import type { MembershipRole } from "./Membership";

export interface Member {
  reader: KnownReader;
  role: MembershipRole;
}
