import { describe, expect, it } from "vitest";
import {
  authorsOf,
  booksAndGapsInOrder,
  booksInOrder,
  publishersOf,
  type Book,
} from "./Book";
import type { SourceAuthor } from "./SourceEdition";

describe("booksInOrder", () => {
  it("sorts the editions by tome", () => {
    expect(
      booksInOrder(
        [tome(10, "Tome 10"), tome(2, "Tome 2"), tome(1, "Tome 1")],
        "fr",
      ).map(({ title }) => title),
    ).toEqual(["Tome 1", "Tome 2", "Tome 10"]);
  });

  it("puts an edition without a tome after the numbered ones", () => {
    expect(
      booksInOrder(
        [tome(null, "Astérix et ses amis"), tome(2, "La Serpe d'or")],
        "fr",
      ).map(({ title }) => title),
    ).toEqual(["La Serpe d'or", "Astérix et ses amis"]);
  });

  it("sorts the editions of one tome by title, ignoring case and accents", () => {
    expect(
      booksInOrder(
        [tome(1, "Asterix the Gaul"), tome(1, "Astérix le Gaulois")],
        "fr",
      ).map(({ title }) => title),
    ).toEqual(["Astérix le Gaulois", "Asterix the Gaul"]);
  });

  it("sorts the editions without a tome by title", () => {
    expect(
      booksInOrder(
        [tome(null, "Le Livre d'or"), tome(null, "Astérix et ses amis")],
        "fr",
      ).map(({ title }) => title),
    ).toEqual(["Astérix et ses amis", "Le Livre d'or"]);
  });
});

describe("booksAndGapsInOrder", () => {
  it("puts each run of missing tomes in its place among the editions", () => {
    // Given
    const books = [
      tome(null, "Astérix et ses amis"),
      tome(5, "Astérix et Cléopâtre"),
      tome(2, "La Serpe d'or"),
    ];

    // When
    const list = booksAndGapsInOrder(books, "fr");

    // Then
    expect(
      list.map((each) => ("book" in each ? each.book.title : each.gap)),
    ).toEqual([
      { first: 1, last: 1 },
      "La Serpe d'or",
      { first: 3, last: 4 },
      "Astérix et Cléopâtre",
      "Astérix et ses amis",
    ]);
  });
});

describe("publishersOf", () => {
  it("names each publisher once", () => {
    // Given
    const books = [
      album(1, "Hachette", []),
      album(2, "Hachette", []),
      album(3, "Orion", []),
    ];

    // When
    const publishers = publishersOf(books, "fr");

    // Then
    expect(publishers).toEqual(["Hachette", "Orion"]);
  });

  it("sorts the publishers by name, ignoring case and accents", () => {
    // Given
    const books = [
      album(1, "Orion", []),
      album(2, "Hachette", []),
      album(3, "Éditions Albert René", []),
    ];

    // When
    const publishers = publishersOf(books, "fr");

    // Then
    expect(publishers).toEqual(["Éditions Albert René", "Hachette", "Orion"]);
  });

  it("adds no publisher for an edition without one", () => {
    // Given
    const books = [album(1, null, []), album(2, "Hachette", [])];

    // When
    const publishers = publishersOf(books, "fr");

    // Then
    expect(publishers).toEqual(["Hachette"]);
  });

  it("reads two spellings of one publisher as one", () => {
    // Given
    const books = [album(1, "Hachette", []), album(2, "HACHETTE", [])];

    // When
    const publishers = publishersOf(books, "fr");

    // Then
    expect(publishers).toEqual(["Hachette"]);
  });

  it("spells a publisher as the first edition a shelf reads", () => {
    // Given
    const books = [album(2, "hachette", []), album(1, "Hachette", [])];

    // When
    const publishers = publishersOf(books, "fr");

    // Then
    expect(publishers).toEqual(["Hachette"]);
  });
});

describe("authorsOf", () => {
  it("names each author of the editions once", () => {
    // Given
    const books = [
      album(1, null, [goscinny, uderzo]),
      album(2, null, [goscinny, uderzo]),
    ];

    // When
    const authors = authorsOf(books, "fr");

    // Then
    expect(authors).toEqual(["René Goscinny", "Albert Uderzo"]);
  });
});

const goscinny: SourceAuthor = { name: "René Goscinny", role: "WRITER" };
const uderzo: SourceAuthor = { name: "Albert Uderzo", role: "ARTIST" };

function tome(volumeNumber: number | null, title: string): Book {
  return {
    id: crypto.randomUUID(),
    isbn13: null,
    kind: "BD",
    title,
    subtitle: null,
    authors: [],
    series: { name: "Astérix", volumeNumber },
    collection: null,
    publisher: null,
    publicationYear: null,
    language: null,
    pageCount: null,
    summary: null,
    coverUrl: null,
    copies: [],
  };
}

function album(
  volumeNumber: number,
  publisher: string | null,
  authors: SourceAuthor[],
): Book {
  return { ...tome(volumeNumber, `Album ${volumeNumber}`), publisher, authors };
}
