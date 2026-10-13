import { describe, expect, inject, it } from "vitest";
import { FetchReadersApi } from "./FetchReadersApi";

describe("FetchReadersApi", () => {
  it("lists the readers Libris knows", async () => {
    // Given
    const api = new FetchReadersApi(inject("mockBaseUrl"));

    // When
    const readers = await api.known();

    // Then
    expect(readers.length).toBeGreaterThan(0);
    for (const reader of readers) {
      expect(reader).toEqual({
        id: expect.any(String),
        displayName: expect.any(String),
      });
    }
  });
});
