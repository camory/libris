import { afterEach, assert, describe, expect, inject, it, vi } from "vitest";
import { FetchIsbnApi } from "./FetchIsbnApi";

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
    expect(answer.edition).toEqual({
      isbn13: expect.any(String),
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
    });
  });

  it("answers a problem when no source knows the ISBN", async () => {
    // Given
    const api = new FetchIsbnApi(inject("mockBaseUrl"));

    // When
    const answer = await api.lookUp("9782000000006");

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
      coverUrl: "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
    });
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
