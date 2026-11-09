import { afterEach, describe, expect, inject, it, vi } from "vitest";
import { FetchAuthorsApi } from "./FetchAuthorsApi";

describe("FetchAuthorsApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

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

  it("fails when Libris answers anything but the names", async () => {
    // Given
    const api = new FetchAuthorsApi("http://libris.invalid");
    vi.stubGlobal("fetch", () =>
      Promise.resolve(
        new Response(JSON.stringify({ status: 500 }), { status: 500 }),
      ),
    );

    // When
    const names = api.names();

    // Then
    await expect(names).rejects.toThrow("The author names answered 500");
  });
});
