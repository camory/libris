import { assert, describe, expect, inject, it } from "vitest";
import { lea } from "../../fixture/Readers";
import { onePiece1 } from "../../fixture/SourceEditions";
import { FetchBookshelfApi } from "./FetchBookshelfApi";

describe("FetchBookshelfApi", () => {
  it("answers the copy now on the bookshelf", async () => {
    // Given
    const api = new FetchBookshelfApi(inject("mockBaseUrl"));

    // When
    const answer = await api.add(lea.defaultBookshelf.id, onePiece1);

    // Then
    assert(answer.outcome === "added", `the answer is a ${answer.outcome}`);
    expect(answer.copy).toEqual({
      id: expect.any(String),
      bookshelf: { id: expect.any(String), name: expect.any(String) },
    });
  });

  it("answers a problem when the API refuses a field", async () => {
    // Given
    const api = new FetchBookshelfApi(inject("mockBaseUrl"));

    // When
    const answer = await api.add(lea.defaultBookshelf.id, {
      ...onePiece1,
      isbn13: "9782723488526",
    });

    // Then
    expect(answer).toEqual({
      outcome: "problem",
      type: "/problems/validation",
    });
  });

  it("answers a problem when the reader owns no such bookshelf", async () => {
    // Given
    const api = new FetchBookshelfApi(inject("mockBaseUrl"));

    // When
    const answer = await api.add(
      "9e8d7c6b-5a4f-4e3d-8c2b-1a0f9e8d7c6b",
      onePiece1,
    );

    // Then
    expect(answer).toEqual({ outcome: "problem", type: "/problems/not-found" });
  });
});
