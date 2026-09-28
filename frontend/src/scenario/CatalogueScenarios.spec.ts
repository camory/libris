import { queries, within, type BoundFunctions } from "@testing-library/dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { App } from "vue";
import {
  catalogueApiKey,
  type BookPage,
  type CatalogueApi,
} from "../application/CatalogueApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Book } from "../domain/Book";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Copy } from "../domain/Copy";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import { FakeBarcodeScanner } from "../fixture/FakeBarcodeScanner";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeCatalogueApi } from "../fixture/FakeCatalogueApi";
import { FakeIsbnApi } from "../fixture/FakeIsbnApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { lea } from "../fixture/Readers";

type Screen = BoundFunctions<typeof queries>;

const maBibliotheque: Bookshelf = {
  id: "0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4e",
  name: "Bibliothèque de Léa",
};

const salon: Bookshelf = {
  id: "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f",
  name: "Salon",
};

describe("Catalogue", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
    vi.unstubAllGlobals();
  });

  it.skip("S1 The catalogue lists the house's editions", async () => {
    // Given
    const catalogue = librisAnswers({
      books: [
        asterix1,
        asterixEtSesAmis,
        { ...onePiece1, copies: [on(maBibliotheque), on(maBibliotheque)] },
      ],
      next: null,
    });

    // When
    const screen = open("/catalogue", catalogue);

    // Then
    await screen.findByText("Parcourir le catalogue");
    await screen.findByText("Romance dawn");
    expect(rows(screen)).toEqual([
      "Astérix · tome 1 Astérix le Gaulois René Goscinny, Albert Uderzo Bibliothèque de Léa",
      "Astérix Astérix et ses amis René Goscinny, Albert Uderzo Salon",
      "One piece · tome 1 Romance dawn Eiichirō Oda Bibliothèque de Léa · 2 exemplaires",
    ]);
  });

  it.skip("S2 The catalogue comes in pages", async () => {
    // Given
    const screen = open(
      "/catalogue",
      librisAnswers(
        { books: [asterix1, asterixEtSesAmis], next: asterixEtSesAmis.id },
        { books: [onePiece1], next: null },
      ),
    );
    await screen.findByText("Astérix et ses amis");

    // When
    theLastRowComesIntoView();

    // Then
    await screen.findByText("Romance dawn");
    expect(rows(screen)).toEqual([
      reads(asterix1),
      reads(asterixEtSesAmis),
      reads(onePiece1),
    ]);
  });

  it("S3 The catalogue is empty", async () => {
    // Given
    const catalogue = librisAnswers({ books: [], next: null });

    // When
    const screen = open("/catalogue", catalogue);

    // Then
    await screen.findByText(
      "Les ouvrages de vos bibliothèques apparaîtront ici.",
    );
    expect(rows(screen)).toEqual([]);
  });

  it.skip("S4 Libris unavailable", async () => {
    // Given
    const catalogue = librisDoesNotAnswer();

    // When
    const screen = open("/catalogue", catalogue);

    // Then
    await screen.findByText(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
    expect(rows(screen)).toEqual([]);
  });

  it.skip("S4 Libris unavailable, on the next page", async () => {
    // Given
    const screen = open(
      "/catalogue",
      librisAnswers(
        { books: [asterix1, asterixEtSesAmis], next: asterixEtSesAmis.id },
        new TypeError("Failed to fetch"),
      ),
    );
    await screen.findByText("Astérix et ses amis");

    // When
    theLastRowComesIntoView();

    // Then
    await screen.findByText(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
    expect(rows(screen)).toEqual([reads(asterix1), reads(asterixEtSesAmis)]);
  });

  const asterix1: Book = book({
    id: "6f1d2c3b-4a59-4e6f-8b70-1c2d3e4f5a61",
    isbn13: "9782012101333",
    kind: "BD",
    title: "Astérix le Gaulois",
    series: { name: "Astérix", volumeNumber: 1 },
    copies: [on(maBibliotheque)],
  });

  const asterixEtSesAmis: Book = book({
    id: "7a2e3d4c-5b6a-4f70-9c81-2d3e4f5a6b72",
    isbn13: "9782226167859",
    kind: "BD",
    title: "Astérix et ses amis",
    series: { name: "Astérix", volumeNumber: null },
    copies: [on(salon)],
  });

  const onePiece1: Book = book({
    id: "5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50",
    isbn13: "9782723488525",
    kind: "MANGA",
    title: "Romance dawn",
    authors: [{ name: "Eiichirō Oda", role: "WRITER" }],
    series: { name: "One piece", volumeNumber: 1 },
    copies: [on(maBibliotheque)],
  });

  function book(fields: Partial<Book> & Pick<Book, "id" | "title">): Book {
    return {
      isbn13: null,
      kind: "BOOK",
      subtitle: null,
      authors: [
        { name: "René Goscinny", role: "WRITER" },
        { name: "Albert Uderzo", role: "ARTIST" },
      ],
      series: null,
      collection: null,
      publisher: null,
      publicationYear: null,
      language: null,
      pageCount: null,
      summary: null,
      coverUrl: null,
      copies: [],
      ...fields,
    };
  }

  function on(bookshelf: Bookshelf): Copy {
    return { id: crypto.randomUUID(), bookshelf };
  }

  function librisAnswers(...pages: (BookPage | Error)[]): CatalogueApi {
    return new FakeCatalogueApi(pages);
  }

  function librisDoesNotAnswer(): CatalogueApi {
    return librisAnswers(new TypeError("Failed to fetch"));
  }

  const observed: {
    callback: IntersectionObserverCallback;
    target: Element;
  }[] = [];

  function theLastRowComesIntoView() {
    const last = observed.at(-1);
    if (last === undefined) throw Error("No row is observed");
    last.callback(
      [
        {
          isIntersecting: true,
          target: last.target,
        } as IntersectionObserverEntry,
      ],
      {} as IntersectionObserver,
    );
  }

  function open(path: string, catalogue: CatalogueApi) {
    observed.length = 0;
    vi.stubGlobal(
      "IntersectionObserver",
      class {
        constructor(private readonly callback: IntersectionObserverCallback) {}
        observe(target: Element) {
          observed.push({ callback: this.callback, target });
        }
        unobserve() {}
        disconnect() {}
      },
    );
    window.history.replaceState(null, "", path);
    app = createLibrisApp(
      {
        meApi: new FakeMeApi(lea),
        isbnApi: new FakeIsbnApi({
          outcome: "problem",
          type: "/problems/not-found",
        }),
        bookshelfApi: new FakeBookshelfApi(
          new Error("no add in this scenario"),
        ),
        barcodeScanner: new FakeBarcodeScanner(false),
        appUpdate: new FakeAppUpdate(),
      },
      "sha-abc1234",
    );
    app.provide(catalogueApiKey, catalogue);
    app.mount(host);
    return within(host);
  }

  function rows(screen: Screen) {
    return screen
      .queryAllByRole("listitem")
      .map((row) => row.textContent?.replace(/\s+/g, " ").trim() ?? "");
  }

  function reads(book: Book) {
    const overline =
      book.series === null
        ? []
        : [
            book.series.volumeNumber === null
              ? book.series.name
              : `${book.series.name} · tome ${book.series.volumeNumber}`,
          ];
    const authors = book.authors.map((author) => author.name).join(", ");
    const bookshelves = [...new Set(book.copies.map((copy) => copy.bookshelf))]
      .map((bookshelf) => {
        const held = book.copies.filter((copy) => copy.bookshelf === bookshelf);
        return held.length > 1
          ? `${bookshelf.name} · ${held.length} exemplaires`
          : bookshelf.name;
      })
      .join(", ");
    return [...overline, book.title, authors, bookshelves].join(" ");
  }
});
