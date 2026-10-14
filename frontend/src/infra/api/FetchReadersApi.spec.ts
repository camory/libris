import { afterEach, describe, expect, inject, it, vi } from "vitest";
import { FetchReadersApi } from "./FetchReadersApi";

describe("FetchReadersApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

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

  it("fails when Libris answers anything but the list", async () => {
    // Given
    const api = new FetchReadersApi("http://libris.invalid");
    vi.stubGlobal("fetch", () =>
      Promise.resolve(
        new Response(JSON.stringify({ status: 500 }), { status: 500 }),
      ),
    );

    // When
    const readers = api.known();

    // Then
    await expect(readers).rejects.toThrow("The readers answered 500");
  });
});
