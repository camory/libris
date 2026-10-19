import { describe, expect, it } from "vitest";
import type { Book } from "./Book";
import { runsOf } from "./Run";

describe("runsOf", () => {
  it("reads a tome alone as a run of its own", () => {
    // Given
    const books = [tome(3), tome(1)];

    // When
    const runs = runsOf(books);

    // Then
    expect(runs).toEqual({
      owned: [
        { first: 1, last: 1 },
        { first: 3, last: 3 },
      ],
      missing: [{ first: 2, last: 2 }],
    });
  });

  it("fills a tome once for two editions of it", () => {
    // Given
    const books = [tome(1), tome(1)];

    // When
    const runs = runsOf(books);

    // Then
    expect(runs).toEqual({ owned: [{ first: 1, last: 1 }], missing: [] });
  });

  it("fills no tome for an edition without one", () => {
    // Given
    const books = [tome(2), tome(null)];

    // When
    const runs = runsOf(books);

    // Then
    expect(runs).toEqual({
      owned: [{ first: 2, last: 2 }],
      missing: [{ first: 1, last: 1 }],
    });
  });
});

function tome(volumeNumber: number | null): Book {
  return {
    id: crypto.randomUUID(),
    isbn13: null,
    kind: "MANGA",
    title: `One piece ${volumeNumber}`,
    subtitle: null,
    authors: [],
    series: { name: "One piece", volumeNumber },
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
