import { describe, expect, inject, it } from "vitest";
import { FetchSeriesApi } from "./FetchSeriesApi";

describe("FetchSeriesApi", () => {
  it("lists the series of the reader's catalogue", async () => {
    // Given
    const api = new FetchSeriesApi(inject("mockBaseUrl"));

    // When
    const series = await api.owned();

    // Then
    expect(series.length).toBeGreaterThan(0);
    for (const each of series) {
      expect(each).toEqual({
        name: expect.any(String),
        volumeCount: expect.any(Number),
        coverUrl: aStringOrNull,
        kind: aKindOrNull,
      });
    }
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
