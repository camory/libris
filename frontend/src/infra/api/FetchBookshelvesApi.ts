import type {
  AddMemberAnswer,
  BookshelvesApi,
  ChooseDefaultAnswer,
  CreateAnswer,
  FindAnswer,
} from "../../application/BookshelvesApi";
import type { Member } from "../../domain/Member";
import type { Membership, MembershipRole } from "../../domain/Membership";
import { problemOf } from "./problemOf";

interface ReaderBookshelfResponse {
  id: string;
  name: string;
  role: MembershipRole;
}

interface BookshelfResponse {
  id: string;
  name: string;
}

interface MemberResponse {
  id: string;
  displayName: string;
  role: MembershipRole;
}

interface BookshelfWithMembersResponse extends BookshelfResponse {
  members: MemberResponse[];
}

const problems = new Map([
  [400, "/problems/validation"],
  [404, "/problems/not-found"],
  [409, "/problems/conflict"],
]);

export class FetchBookshelvesApi implements BookshelvesApi {
  constructor(private readonly baseUrl: string) {}

  async memberships(): Promise<Membership[]> {
    const response = await fetch(`${this.baseUrl}/api/v1/bookshelves`, {
      headers: { Accept: "application/json, application/problem+json" },
    });
    if (response.status !== 200) {
      throw Error(`The bookshelves answered ${response.status}`);
    }
    const body = (await response.json()) as ReaderBookshelfResponse[];
    return body.map((bookshelf) => ({
      bookshelf: { id: bookshelf.id, name: bookshelf.name },
      role: bookshelf.role,
    }));
  }

  async create(name: string): Promise<CreateAnswer> {
    const response = await fetch(`${this.baseUrl}/api/v1/bookshelves`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json, application/problem+json",
        "X-Requested-With": "XMLHttpRequest",
      },
      body: JSON.stringify({ name }),
    });
    if (response.status === 201) {
      const body = (await response.json()) as BookshelfResponse;
      return {
        outcome: "created",
        bookshelf: { id: body.id, name: body.name },
      };
    }
    return problemOf(response, problems);
  }

  async find(id: string): Promise<FindAnswer> {
    const response = await fetch(
      `${this.baseUrl}/api/v1/bookshelves/${encodeURIComponent(id)}`,
      { headers: { Accept: "application/json, application/problem+json" } },
    );
    if (response.status !== 200) return problemOf(response, problems);
    const body = (await response.json()) as BookshelfWithMembersResponse;
    return {
      outcome: "found",
      bookshelf: { id: body.id, name: body.name },
      members: body.members.map(memberOf),
    };
  }

  async addMember(
    bookshelfId: string,
    readerId: string,
    role: MembershipRole,
  ): Promise<AddMemberAnswer> {
    const response = await fetch(
      `${this.baseUrl}/api/v1/bookshelves/${encodeURIComponent(bookshelfId)}/members`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json, application/problem+json",
        },
        body: JSON.stringify({ readerId, role }),
      },
    );
    const body = (await response.json()) as MemberResponse;
    return { outcome: "added", member: memberOf(body) };
  }

  chooseDefault(): Promise<ChooseDefaultAnswer> {
    return Promise.reject(new Error("chooseDefault is not implemented"));
  }
}

function memberOf(member: MemberResponse): Member {
  return {
    reader: { id: member.id, displayName: member.displayName },
    role: member.role,
  };
}
