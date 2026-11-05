import type {
  BookPage,
  CatalogueApi,
  FindBookAnswer,
} from "../application/CatalogueApi";

const notFound = { outcome: "problem", type: "/problems/not-found" } as const;

export class FakeCatalogueApi implements CatalogueApi {
  readonly asked: (string | null)[] = [];
  private readonly finds: (FindBookAnswer | Error)[];

  constructor(
    private readonly pages: (BookPage | Error)[],
    found: FindBookAnswer | Error | (FindBookAnswer | Error)[] = new Error(
      "no find in this scenario",
    ),
  ) {
    this.finds = Array.isArray(found) ? [...found] : [found];
  }

  browse(after: string | null): Promise<BookPage> {
    this.asked.push(after);
    const page = this.pages.shift();
    if (page === undefined) return Promise.reject(Error("No page left"));
    return page instanceof Error ? Promise.reject(page) : Promise.resolve(page);
  }

  find(id: string): Promise<FindBookAnswer> {
    const found = this.finds.length > 1 ? this.finds.shift() : this.finds.at(0);
    if (found === undefined) return Promise.reject(Error("No find left"));
    if (found instanceof Error) return Promise.reject(found);
    if (found.outcome === "problem") return Promise.resolve(found);
    if (found.book.id !== id) return Promise.resolve(notFound);
    return Promise.resolve({
      outcome: "found",
      book: { ...found.book, copies: [...found.book.copies] },
    });
  }
}
