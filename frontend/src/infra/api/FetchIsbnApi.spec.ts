import { afterEach, assert, describe, expect, inject, it, vi } from "vitest";
import {
  aKind,
  aNumberOrNull,
  aSeriesOrNull,
  aStringOrNull,
  authorsWithNameAndRole,
  candidatesWithSourceAndUrl,
} from "../../fixture/EditionShapes";
import { FetchIsbnApi } from "./FetchIsbnApi";

describe("FetchIsbnApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("answers what the sources know about the ISBN", async () => {
    // Given
    const api = new FetchIsbnApi(inject("mockBaseUrl"));

    // When
    const answer = await api.lookUp("9782723488525");

    // Then
    assert(answer.outcome === "found", `the answer is a ${answer.outcome}`);
    expect(answer.id).toEqual(aStringOrNull);
    expect(answer.covers).toEqual(candidatesWithSourceAndUrl);
    expect(answer.edition).toEqual({
      isbn13: expect.any(String),
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
    });
  });

  it("answers the copies on the reader's bookshelves beside the edition", async () => {
    // Given
    const api = new FetchIsbnApi(inject("mockBaseUrl"));

    // When
    const answer = await api.lookUp("9782723489898");

    // Then
    assert(answer.outcome === "found", `the answer is a ${answer.outcome}`);
    expect(answer.copies).toSatisfy(
      areCopies,
      "copies with an id and a bookshelf",
    );
  });

  it("answers a problem when no source knows the ISBN", async () => {
    // Given
    const api = new FetchIsbnApi(inject("mockBaseUrl"));

    // When
    const answer = await api.lookUp("9782000000013");

    // Then
    expect(answer).toEqual({ outcome: "problem", type: "/problems/not-found" });
  });

  it("answers a problem when no source answered", async () => {
    // Given
    const api = new FetchIsbnApi(inject("mockBaseUrl"));

    // When
    const answer = await api.lookUp("9791000000008");

    // Then
    expect(answer).toEqual({
      outcome: "problem",
      type: "/problems/sources-unavailable",
    });
  });

  it("answers a problem when the API refuses the ISBN", async () => {
    // Given
    const api = new FetchIsbnApi(inject("mockBaseUrl"));

    // When
    const answer = await api.lookUp("9782723488526");

    // Then
    expect(answer).toEqual({
      outcome: "problem",
      type: "/problems/validation",
    });
  });

  it("reads the fields it knows when the answer carries one it does not", async () => {
    // Given
    const api = new FetchIsbnApi("http://an-older-backend");
    const body = {
      isbn13: "9782723488525",
      kind: "MANGA",
      title: "Romance dawn",
      subtitle: "à l'aube d'une grande aventure",
      authors: [
        { name: "Eiichirō Oda", role: "WRITER" },
        { name: "Eiichirō Oda", role: "ARTIST" },
      ],
      series: { name: "One piece", volumeNumber: 1 },
      collection: "Shonen manga",
      publisher: "Glénat",
      publicationYear: 2013,
      language: "fr",
      pageCount: 203,
      summary: null,
      coverUrl: "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
      copies: [],
      id: null,
      covers: [],
      sources: ["BNF", "OPEN_LIBRARY"],
    };
    vi.stubGlobal("fetch", () =>
      Promise.resolve(new Response(JSON.stringify(body), { status: 200 })),
    );

    // When
    const answer = await api.lookUp("9782723488525");

    // Then
    assert(answer.outcome === "found", `the answer is a ${answer.outcome}`);
    expect(answer.edition).toMatchObject({
      isbn13: "9782723488525",
      title: "Romance dawn",
      authors: [
        { name: "Eiichirō Oda", role: "WRITER" },
        { name: "Eiichirō Oda", role: "ARTIST" },
      ],
    });
  });
});

function areCopies(value: unknown) {
  return (
    Array.isArray(value) &&
    value.length > 0 &&
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
