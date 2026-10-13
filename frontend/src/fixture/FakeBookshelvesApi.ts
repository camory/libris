import type {
  AddMemberAnswer,
  BookshelvesApi,
  ChooseDefaultAnswer,
  CreateAnswer,
  FindAnswer,
} from "../application/BookshelvesApi";
import type { Bookshelf } from "../domain/Bookshelf";
import type { KnownReader } from "../domain/KnownReader";
import type { Membership, MembershipRole } from "../domain/Membership";

const notFound = { outcome: "problem", type: "/problems/not-found" } as const;

const invalid = { outcome: "problem", type: "/problems/validation" } as const;

export class FakeBookshelvesApi implements BookshelvesApi {
  private readonly held: Membership[] | Error;
  private readonly found: FindAnswer | Error;

  constructor(
    private readonly answers: {
      memberships: Membership[] | Error;
      create?: CreateAnswer | Error;
      find?: FindAnswer | Error;
      adds?: KnownReader | Error;
      defaultChosen?: ((bookshelf: Bookshelf) => void) | Error;
    },
  ) {
    this.held =
      answers.memberships instanceof Error
        ? answers.memberships
        : [...answers.memberships];
    const find = answers.find ?? new Error("no find in this scenario");
    this.found =
      find instanceof Error || find.outcome === "problem"
        ? find
        : { ...find, members: [...find.members] };
  }

  memberships(): Promise<Membership[]> {
    return this.held instanceof Error
      ? Promise.reject(this.held)
      : Promise.resolve([...this.held]);
  }

  create(): Promise<CreateAnswer> {
    const answer =
      this.answers.create ?? new Error("no create in this scenario");
    if (answer instanceof Error) return Promise.reject(answer);
    if (answer.outcome === "created" && !(this.held instanceof Error))
      this.held.push({ bookshelf: answer.bookshelf, role: "OWNER" });
    return Promise.resolve(answer);
  }

  find(id: string): Promise<FindAnswer> {
    if (this.found instanceof Error) return Promise.reject(this.found);
    if (this.found.outcome === "problem") return Promise.resolve(this.found);
    if (this.found.bookshelf.id !== id) return Promise.resolve(notFound);
    return Promise.resolve({
      ...this.found,
      members: [...this.found.members],
    });
  }

  addMember(
    bookshelfId: string,
    readerId: string,
    role: MembershipRole,
  ): Promise<AddMemberAnswer> {
    const reader = this.answers.adds ?? new Error("no add in this scenario");
    if (reader instanceof Error) return Promise.reject(reader);
    if (this.found instanceof Error || this.found.outcome === "problem")
      return Promise.resolve(notFound);
    if (this.found.bookshelf.id !== bookshelfId)
      return Promise.resolve(notFound);
    if (reader.id !== readerId) return Promise.resolve(invalid);
    const member = { reader, role };
    this.found.members.push(member);
    return Promise.resolve({ outcome: "added", member });
  }

  chooseDefault(id: string): Promise<ChooseDefaultAnswer> {
    const chosen =
      this.answers.defaultChosen ?? new Error("no choice in this scenario");
    if (chosen instanceof Error) return Promise.reject(chosen);
    if (this.held instanceof Error) return Promise.resolve(notFound);
    const owned = this.held.find(
      ({ bookshelf, role }) => bookshelf.id === id && role === "OWNER",
    );
    if (owned === undefined) return Promise.resolve(notFound);
    chosen(owned.bookshelf);
    return Promise.resolve({ outcome: "chosen" });
  }
}
