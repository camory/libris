import { afterEach, describe, expect, inject, it, vi } from "vitest";
import {
  aKind,
  aNumberOrNull,
  aSeriesOrNull,
  aStringOrNull,
  authorsWithNameAndRole,
} from "../../fixture/EditionShapes";
import { FetchCatalogueApi } from "./FetchCatalogueApi";

describe("FetchCatalogueApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

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
        authors: authorsWithNameAndRole,
        series: aSeriesOrNull,
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

  it("asks for the page after the given book", async () => {
    // Given
    const api = new FetchCatalogueApi("http://libris.invalid");
    const requested: string[] = [];
    vi.stubGlobal("fetch", (url: string) => {
      requested.push(url);
      return Promise.resolve(
        new Response(JSON.stringify({ books: [], next: null }), {
          status: 200,
        }),
      );
    });

    // When
    await api.browse("5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50");

    // Then
    expect(requested).toEqual([
      "http://libris.invalid/api/v1/books?after=5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50",
    ]);
  });

  it("fails when the API refuses the page asked", async () => {
    // Given
    const api = new FetchCatalogueApi(inject("mockBaseUrl"));

    // When
    const page = api.browse("not-an-id");

    // Then
    await expect(page).rejects.toThrow();
  });
});

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
