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
