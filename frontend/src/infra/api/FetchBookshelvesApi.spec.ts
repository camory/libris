import { describe, expect, inject, it } from "vitest";
import { FetchBookshelvesApi } from "./FetchBookshelvesApi";

describe("FetchBookshelvesApi", () => {
  it("lists the reader's bookshelves with their role", async () => {
    // Given
    const api = new FetchBookshelvesApi(inject("mockBaseUrl"));

    // When
    const memberships = await api.memberships();

    // Then
    expect(memberships.length).toBeGreaterThan(0);
    for (const membership of memberships) {
      expect(membership).toEqual({
        bookshelf: { id: expect.any(String), name: expect.any(String) },
        role: expect.stringMatching(/^(OWNER|VIEWER)$/),
      });
    }
  });
});
