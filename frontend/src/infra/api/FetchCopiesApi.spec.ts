import { afterEach, describe, expect, inject, it, vi } from "vitest";
import { FetchCopiesApi } from "./FetchCopiesApi";

describe("FetchCopiesApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("removes the copy", async () => {
    // Given
    const api = new FetchCopiesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.remove("4d5e6f70-8192-4a3b-9c4d-5e6f708192a3");

    // Then
    expect(answer).toEqual({ outcome: "removed" });
  });

  it("answers a problem when the reader owns no such copy", async () => {
    // Given
    const api = new FetchCopiesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.remove("8b9c0d1e-2f3a-4b4c-9d5e-6f7a8b9c0d1e");

    // Then
    expect(answer).toEqual({ outcome: "problem", type: "/problems/not-found" });
  });

  it("answers a problem when the id is not one", async () => {
    // Given
    const api = new FetchCopiesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.remove("not-a-uuid");

    // Then
    expect(answer).toEqual({
      outcome: "problem",
      type: "/problems/validation",
    });
  });

  it("says the removal comes from the application", async () => {
    // Given
    const api = new FetchCopiesApi("http://libris.invalid");
    const sent: RequestInit[] = [];
    vi.stubGlobal("fetch", (_url: string, init: RequestInit) => {
      sent.push(init);
      return Promise.resolve(new Response(null, { status: 204 }));
    });

    // When
    await api.remove("4d5e6f70-8192-4a3b-9c4d-5e6f708192a3");

    // Then
    expect(sent[0]).toMatchObject({
      method: "DELETE",
      headers: { "X-Requested-With": "XMLHttpRequest" },
    });
  });
});
