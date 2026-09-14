import { assert, describe, expect, inject, it } from "vitest";
import { FetchIsbnApi } from "./FetchIsbnApi";

const aStringOrNull = expect.toSatisfy(
  (value: unknown) => value === null || typeof value === "string",
  "a string or null",
);
const aNumberOrNull = expect.toSatisfy(
  (value: unknown) => value === null || typeof value === "number",
  "a number or null",
);

describe("FetchIsbnApi", () => {
  it("answers what the sources know about the ISBN", async () => {
    // Given
    const api = new FetchIsbnApi(inject("mockBaseUrl"));

    // When
    const answer = await api.lookUp("9782723488525");

    // Then
    assert(answer.outcome === "found", `the answer is a ${answer.outcome}`);
    expect(answer.edition).toEqual({
      isbn13: expect.any(String),
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
      sources: expect.toSatisfy(areSources, "a non-empty array of sources"),
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

function areSources(value: unknown) {
  return (
    Array.isArray(value) &&
    value.length > 0 &&
    value.every((source) => ["BNF", "OPEN_LIBRARY"].includes(source))
  );
}
