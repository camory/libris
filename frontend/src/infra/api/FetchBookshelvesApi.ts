import type {
  AddMemberAnswer,
  BookshelvesApi,
  ChooseDefaultAnswer,
  CreateAnswer,
  FindAnswer,
} from "../../application/BookshelvesApi";
import type { Membership, MembershipRole } from "../../domain/Membership";

interface ReaderBookshelfResponse {
  id: string;
  name: string;
  role: MembershipRole;
}

interface BookshelfResponse {
  id: string;
  name: string;
}

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
      },
      body: JSON.stringify({ name }),
    });
    const body = (await response.json()) as BookshelfResponse;
    return {
      outcome: "created",
      bookshelf: { id: body.id, name: body.name },
    };
  }

  find(): Promise<FindAnswer> {
    return Promise.reject(new Error("find is not implemented"));
  }

  addMember(): Promise<AddMemberAnswer> {
    return Promise.reject(new Error("addMember is not implemented"));
  }

  chooseDefault(): Promise<ChooseDefaultAnswer> {
    return Promise.reject(new Error("chooseDefault is not implemented"));
  }
}
