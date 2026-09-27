import { queries, within, type BoundFunctions } from "@testing-library/dom";
import { afterEach, describe, expect, inject, it, vi } from "vitest";
import { bootstrap } from "../bootstrap";

type Screen = BoundFunctions<typeof queries>;

interface Bookshelf {
  id: string;
  name: string;
}

interface Copy {
  id: string;
  bookshelf: Bookshelf;
}

interface Book {
  id: string;
  isbn13: string | null;
  kind: "BOOK" | "BD" | "MANGA";
  title: string;
  subtitle: string | null;
  authors: { name: string; role: "WRITER" | "ARTIST" | "COLOURIST" | "TRANSLATOR" }[];
  series: { name: string; volumeNumber: number | null } | null;
  collection: string | null;
  publisher: string | null;
  publicationYear: number | null;
  language: string | null;
  pageCount: number | null;
  summary: string | null;
  coverUrl: string | null;
  copies: Copy[];
}

interface BookPage {
  books: Book[];
  next: string | null;
}

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
  let app: ReturnType<typeof bootstrap>;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it.skip("S1 The catalogue lists the house's editions", async () => {
    // Given
    librisAnswers({
      books: [
        asterix1,
        asterixEtSesAmis,
        { ...onePiece1, copies: [on(maBibliotheque), on(maBibliotheque)] },
      ],
      next: null,
    });

    // When
    const screen = open("/catalogue");

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
    librisAnswers(
      { books: [asterix1, asterixEtSesAmis], next: asterixEtSesAmis.id },
      { books: [onePiece1], next: null },
    );
    const screen = open("/catalogue");
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

  it.skip("S3 The catalogue is empty", async () => {
    // Given
    librisAnswers({ books: [], next: null });

    // When
    const screen = open("/catalogue");

    // Then
    await screen.findByText(
      "Les ouvrages de vos bibliothèques apparaîtront ici.",
    );
    expect(rows(screen)).toEqual([]);
  });

  it.skip("S4 Libris unavailable", async () => {
    // Given
    librisDoesNotAnswer();

    // When
    const screen = open("/catalogue");

    // Then
    await screen.findByText(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
    expect(rows(screen)).toEqual([]);
  });

  it.skip("S4 Libris unavailable, on the next page", async () => {
    // Given
    librisAnswers(
      { books: [asterix1, asterixEtSesAmis], next: asterixEtSesAmis.id },
      Error("Failed to fetch"),
    );
    const screen = open("/catalogue");
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

  function librisAnswers(...pages: (BookPage | Error)[]) {
    const fetch = globalThis.fetch;
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) => {
      if (!String(input).includes("/api/v1/books")) return fetch(input, init);
      const page = pages.shift();
      if (page === undefined) throw Error("No page left to answer");
      return page instanceof Error
        ? Promise.reject(page)
        : Promise.resolve(
            new Response(JSON.stringify(page), {
              status: 200,
              headers: { "Content-Type": "application/json" },
            }),
          );
    });
  }

  function librisDoesNotAnswer() {
    librisAnswers(Error("Failed to fetch"));
  }

  const observed: { callback: IntersectionObserverCallback; target: Element }[] =
    [];

  function theLastRowComesIntoView() {
    const last = observed.at(-1);
    if (last === undefined) throw Error("No row is observed");
    last.callback(
      [{ isIntersecting: true, target: last.target } as IntersectionObserverEntry],
      {} as IntersectionObserver,
    );
  }

  function open(path: string) {
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
    app = bootstrap(inject("mockBaseUrl"), "sha-abc1234");
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
