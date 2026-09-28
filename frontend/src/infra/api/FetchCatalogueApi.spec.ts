import { describe, expect, inject, it } from "vitest";
import { FetchCatalogueApi } from "./FetchCatalogueApi";

const aStringOrNull = expect.toSatisfy(
  (value: unknown) => value === null || typeof value === "string",
  "a string or null",
);
const aNumberOrNull = expect.toSatisfy(
  (value: unknown) => value === null || typeof value === "number",
  "a number or null",
);
const aKind = expect.toSatisfy(
  (value: unknown) => ["BOOK", "BD", "MANGA"].includes(value as string),
  "one of the kinds the API answers",
);

describe("FetchCatalogueApi", () => {
  it("answers a page of the reader's catalogue", async () => {
    // Given
    const api = new FetchCatalogueApi(inject("mockBaseUrl"));

    // When
    const page = await api.browse(null);

    // Then
    expect(page.books).toBeInstanceOf(Array);
    for (const book of page.books) {
      expect(book).toEqual({
        id: expect.any(String),
        isbn13: aStringOrNull,
        kind: aKind,
        title: expect.any(String),
        subtitle: aStringOrNull,
        authors: expect.toSatisfy(areAuthors, "authors with a name and a role"),
        series: expect.toSatisfy(isSeriesOrNull, "a series or null"),
        collection: aStringOrNull,
        publisher: aStringOrNull,
        publicationYear: aNumberOrNull,
        language: aStringOrNull,
        pageCount: aNumberOrNull,
        summary: aStringOrNull,
        coverUrl: aStringOrNull,
        copies: expect.toSatisfy(
          areCopies,
          "copies with an id and a bookshelf",
        ),
      });
    }
    expect(page.next).toEqual(aStringOrNull);
  });
});

function areAuthors(value: unknown) {
  return (
    Array.isArray(value) &&
    value.every(
      (author) =>
        typeof author.name === "string" &&
        ["WRITER", "ARTIST", "COLOURIST", "TRANSLATOR"].includes(author.role),
    )
  );
}

function areCopies(value: unknown) {
  return (
    Array.isArray(value) &&
    value.every(
      (copy) =>
        Object.keys(copy).sort().join() === "bookshelf,id" &&
        typeof copy.id === "string" &&
        Object.keys(copy.bookshelf).sort().join() === "id,name" &&
        typeof copy.bookshelf.id === "string" &&
        typeof copy.bookshelf.name === "string",
    )
  );
}

function isSeriesOrNull(value: unknown) {
  if (value === null) {
    return true;
  }
  const series = value as { name: unknown; volumeNumber: unknown };
  return (
    typeof series.name === "string" &&
    (series.volumeNumber === null || typeof series.volumeNumber === "number")
  );
}
