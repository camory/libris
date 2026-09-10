import { describe, expect, inject, it } from "vitest";
import { FetchMeApi } from "./FetchMeApi";

describe("FetchMeApi", () => {
  it("answers the reader the API describes", async () => {
    // Given
    const api = new FetchMeApi(inject("mockBaseUrl"));

    // When
    const reader = await api.currentReader();

    // Then
    expect(reader.id).toEqual(expect.any(String));
    expect(reader.username).toEqual(expect.any(String));
    expect(reader.displayName).toEqual(expect.any(String));
    expect(reader.email).toEqual(expect.any(String));
    expect(["READER", "ADMIN"]).toContain(reader.role);
  });
});
