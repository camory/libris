import type { BookPage, CatalogueApi } from "../application/CatalogueApi";

export class FakeCatalogueApi implements CatalogueApi {
  readonly asked: (string | null)[] = [];

  constructor(private readonly pages: (BookPage | Error)[]) {}

  browse(after: string | null): Promise<BookPage> {
    this.asked.push(after);
    const page = this.pages.shift();
    if (page === undefined) return Promise.reject(Error("No page left"));
    return page instanceof Error ? Promise.reject(page) : Promise.resolve(page);
  }
}
