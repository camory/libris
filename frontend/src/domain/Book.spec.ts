import { describe, expect, it } from "vitest";
import { booksInOrder, type Book } from "./Book";

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
