import { describe, expect, inject, it } from "vitest";
import { FetchAuthorsApi } from "./FetchAuthorsApi";

describe("FetchAuthorsApi", () => {
  it("answers the names of the house's authors", async () => {
    // Given
    const api = new FetchAuthorsApi(inject("mockBaseUrl"));

    // When
    const names = await api.names();

    // Then
    expect(names.length).toBeGreaterThan(0);
    for (const name of names) {
      expect(name).toEqual(expect.stringMatching(/./));
    }
  });
});
