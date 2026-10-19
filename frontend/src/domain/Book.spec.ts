import { describe, expect, it } from "vitest";
import { booksAndGapsInOrder, booksInOrder, type Book } from "./Book";

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
