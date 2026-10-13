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

export class FetchBookshelvesApi implements BookshelvesApi {
  constructor(private readonly baseUrl: string) {}

  async memberships(): Promise<Membership[]> {
    const response = await fetch(`${this.baseUrl}/api/v1/bookshelves`, {
      headers: { Accept: "application/json, application/problem+json" },
    });
    const body = (await response.json()) as ReaderBookshelfResponse[];
    return body.map((bookshelf) => ({
      bookshelf: { id: bookshelf.id, name: bookshelf.name },
      role: bookshelf.role,
    }));
  }

  create(): Promise<CreateAnswer> {
    return Promise.reject(new Error("create is not implemented"));
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
