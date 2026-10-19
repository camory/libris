import { afterEach, describe, expect, inject, it, vi } from "vitest";
import { aBook } from "../../fixture/EditionShapes";
import { FetchSeriesApi } from "./FetchSeriesApi";

describe("FetchSeriesApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("lists the series of the reader's catalogue", async () => {
    // Given
    const api = new FetchSeriesApi(inject("mockBaseUrl"));

    // When
    const series = await api.owned();

    // Then
    expect(series.length).toBeGreaterThan(0);
    for (const each of series) {
      expect(each).toEqual({
        id: expect.any(String),
        name: expect.any(String),
        volumeCount: expect.any(Number),
        coverUrl: aStringOrNull,
        kind: aKindOrNull,
      });
    }
  });

  it("fails when Libris answers anything but the list", async () => {
    // Given
    const api = new FetchSeriesApi("http://libris.invalid");
    vi.stubGlobal("fetch", () =>
      Promise.resolve(
        new Response(JSON.stringify({ status: 500 }), { status: 500 }),
      ),
    );

    // When
    const series = api.owned();

    // Then
    await expect(series).rejects.toThrow("The series answered 500");
  });

  it("finds the series with its books", async () => {
    // Given
    const api = new FetchSeriesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.find("1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f");

    // Then
    expect(answer).toEqual({
      outcome: "found",
      series: {
        id: expect.any(String),
        name: expect.any(String),
        volumeCount: expect.any(Number),
        coverUrl: aStringOrNull,
        kind: aKindOrNull,
        books: expect.any(Array),
      },
    });
    const books = answer.outcome === "found" ? answer.series.books : [];
    expect(books.length).toBeGreaterThan(0);
    for (const book of books) {
      expect(book).toEqual(aBook);
    }
  });

  it("answers a problem when no edition of the series is on the reader's bookshelves", async () => {
    // Given
    const api = new FetchSeriesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.find("5b6c7d8e-9f0a-4b1c-8d2e-3f4a5b6c7d8e");

    // Then
    expect(answer).toEqual({ outcome: "problem", type: "/problems/not-found" });
  });

  it("answers a problem when the id is not one", async () => {
    // Given
    const api = new FetchSeriesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.find("not-a-uuid");

    // Then
    expect(answer).toEqual({
      outcome: "problem",
      type: "/problems/validation",
    });
  });
});

const aStringOrNull = expect.toSatisfy(
  (value: unknown) => value === null || typeof value === "string",
  "a string or null",
);

const aKindOrNull = expect.toSatisfy(
  (value: unknown) =>
    value === null || value === "BOOK" || value === "BD" || value === "MANGA",
  "a kind or null",
);
