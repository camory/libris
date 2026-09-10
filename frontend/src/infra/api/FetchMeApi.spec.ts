import { afterEach, describe, expect, inject, it, vi } from "vitest";
import { FetchMeApi } from "./FetchMeApi";

describe("FetchMeApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("answers the reader the API describes", async () => {
    // Given
    const api = new FetchMeApi(inject("mockBaseUrl"), () => {});

    // When
    const reader = await api.currentReader();

    // Then
    expect(reader.id).toEqual(expect.any(String));
    expect(reader.username).toEqual(expect.any(String));
    expect(reader.displayName).toEqual(expect.any(String));
    expect(reader.email).toEqual(expect.any(String));
    expect(["READER", "ADMIN"]).toContain(reader.role);
  });

  it("reports an expired session and answers nothing", async () => {
    // Given
    const onUnauthenticated = vi.fn();
    const api = new FetchMeApi(inject("mockBaseUrl"), onUnauthenticated);
    vi.stubGlobal("fetch", () =>
      Promise.resolve(new Response(null, { status: 401 })),
    );

    // When
    const answer = api.currentReader();

    // Then
    await expect(answer).rejects.toThrow();
    expect(onUnauthenticated).toHaveBeenCalledOnce();
  });
});
