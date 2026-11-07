import { describe, expect, inject, it } from "vitest";
import { FetchCopiesApi } from "./FetchCopiesApi";

describe("FetchCopiesApi", () => {
  it("removes the copy", async () => {
    // Given
    const api = new FetchCopiesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.remove("4d5e6f70-8192-4a3b-9c4d-5e6f708192a3");

    // Then
    expect(answer).toEqual({ outcome: "removed" });
  });
});
