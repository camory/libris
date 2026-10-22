import type {
  BookPage,
  CatalogueApi,
  FindBookAnswer,
} from "../application/CatalogueApi";

const notFound = { outcome: "problem", type: "/problems/not-found" } as const;

export class FakeCatalogueApi implements CatalogueApi {
  readonly asked: (string | null)[] = [];

  constructor(
    private readonly pages: (BookPage | Error)[],
    private readonly found: FindBookAnswer | Error = new Error(
      "no find in this scenario",
    ),
  ) {}

  browse(after: string | null): Promise<BookPage> {
    this.asked.push(after);
    const page = this.pages.shift();
    if (page === undefined) return Promise.reject(Error("No page left"));
    return page instanceof Error ? Promise.reject(page) : Promise.resolve(page);
  }

  find(id: string): Promise<FindBookAnswer> {
    if (this.found instanceof Error) return Promise.reject(this.found);
    if (this.found.outcome === "problem") return Promise.resolve(this.found);
    if (this.found.book.id !== id) return Promise.resolve(notFound);
    return Promise.resolve({
      outcome: "found",
      book: { ...this.found.book, copies: [...this.found.book.copies] },
    });
  }
}
