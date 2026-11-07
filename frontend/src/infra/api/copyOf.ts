import type { Copy } from "../../domain/Copy";
import type { MembershipRole } from "../../domain/Membership";

export interface CopyResponse {
  id: string;
  bookshelf: { id: string; name: string; role: MembershipRole };
}

export function copyOf(copy: CopyResponse): Copy {
  return {
    id: copy.id,
    bookshelf: {
      id: copy.bookshelf.id,
      name: copy.bookshelf.name,
      role: copy.bookshelf.role,
    },
  };
}
