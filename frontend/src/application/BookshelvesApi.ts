import type { InjectionKey } from "vue";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Member } from "../domain/Member";
import type { Membership, MembershipRole } from "../domain/Membership";

export type CreateAnswer =
  | { outcome: "created"; bookshelf: Bookshelf }
  | { outcome: "problem"; type: string };

export type FindAnswer =
  | { outcome: "found"; bookshelf: Bookshelf; members: Member[] }
  | { outcome: "problem"; type: string };

export type AddMemberAnswer =
  { outcome: "added"; member: Member } | { outcome: "problem"; type: string };

export type ChooseDefaultAnswer =
  { outcome: "chosen" } | { outcome: "problem"; type: string };

export interface BookshelvesApi {
  memberships(): Promise<Membership[]>;
  create(name: string): Promise<CreateAnswer>;
  find(id: string): Promise<FindAnswer>;
  addMember(
    bookshelfId: string,
    readerId: string,
    role: MembershipRole,
  ): Promise<AddMemberAnswer>;
  chooseDefault(id: string): Promise<ChooseDefaultAnswer>;
}

export const bookshelvesApiKey: InjectionKey<BookshelvesApi> =
  Symbol("BookshelvesApi");
