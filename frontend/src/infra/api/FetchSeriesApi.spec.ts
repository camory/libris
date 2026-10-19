import { afterEach, describe, expect, inject, it, vi } from "vitest";
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
