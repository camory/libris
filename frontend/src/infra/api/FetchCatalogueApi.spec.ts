import { afterEach, describe, expect, inject, it, vi } from "vitest";
import { aBook, aStringOrNull } from "../../fixture/EditionShapes";
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
      expect(book).toEqual(aBook);
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

  it("finds the ouvrage with its copies", async () => {
    // Given
    const api = new FetchCatalogueApi(inject("mockBaseUrl"));

    // When
    const answer = await api.find("1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f");

    // Then
    expect(answer).toEqual({ outcome: "found", book: aBook });
  });

  it("answers a problem when no copy is on the reader's bookshelves", async () => {
    // Given
    const api = new FetchCatalogueApi(inject("mockBaseUrl"));

    // When
    const answer = await api.find("3e4f5a6b-7c8d-4e9f-a0b1-c2d3e4f5a6b7");

    // Then
    expect(answer).toEqual({ outcome: "problem", type: "/problems/not-found" });
  });

  it("answers a problem when the id is not one", async () => {
    // Given
    const api = new FetchCatalogueApi(inject("mockBaseUrl"));

    // When
    const answer = await api.find("not-a-uuid");

    // Then
    expect(answer).toEqual({
      outcome: "problem",
      type: "/problems/validation",
    });
  });
});
