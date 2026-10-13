import { afterEach, describe, expect, inject, it, vi } from "vitest";
import { FetchBookshelvesApi } from "./FetchBookshelvesApi";

describe("FetchBookshelvesApi", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

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

  it("fails when Libris answers anything but the list", async () => {
    // Given
    const api = new FetchBookshelvesApi("http://libris.invalid");
    vi.stubGlobal("fetch", () =>
      Promise.resolve(
        new Response(JSON.stringify({ status: 500 }), { status: 500 }),
      ),
    );

    // When
    const memberships = api.memberships();

    // Then
    await expect(memberships).rejects.toThrow("The bookshelves answered 500");
  });

  it("creates the bookshelf named", async () => {
    // Given
    const api = new FetchBookshelvesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.create("Bibliothèque familiale");

    // Then
    expect(answer).toEqual({
      outcome: "created",
      bookshelf: { id: expect.any(String), name: expect.any(String) },
    });
  });

  it("answers a problem when the name is blank", async () => {
    // Given
    const api = new FetchBookshelvesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.create("   ");

    // Then
    expect(answer).toEqual({
      outcome: "problem",
      type: "/problems/validation",
    });
  });

  it("answers a problem when the name is taken", async () => {
    // Given
    const api = new FetchBookshelvesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.create("Salon");

    // Then
    expect(answer).toEqual({ outcome: "problem", type: "/problems/conflict" });
  });

  it("says the creation comes from the application", async () => {
    // Given
    const api = new FetchBookshelvesApi("http://libris.invalid");
    const sent: RequestInit[] = [];
    vi.stubGlobal("fetch", (_url: string, init: RequestInit) => {
      sent.push(init);
      return Promise.resolve(
        new Response(
          JSON.stringify({
            id: "4f5a6b7c-8d9e-4f0a-9b2c-3d4e5f6a7b82",
            name: "Bibliothèque familiale",
          }),
          { status: 201 },
        ),
      );
    });

    // When
    await api.create("Bibliothèque familiale");

    // Then
    expect(sent[0]?.headers).toMatchObject({
      "X-Requested-With": "XMLHttpRequest",
    });
  });

  it("finds the bookshelf with its members", async () => {
    // Given
    const api = new FetchBookshelvesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.find("1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f");

    // Then
    expect(answer).toEqual({
      outcome: "found",
      bookshelf: { id: expect.any(String), name: expect.any(String) },
      members: expect.any(Array),
    });
    const members = answer.outcome === "found" ? answer.members : [];
    expect(members.length).toBeGreaterThan(0);
    for (const member of members) {
      expect(member).toEqual({
        reader: { id: expect.any(String), displayName: expect.any(String) },
        role: expect.stringMatching(/^(OWNER|VIEWER)$/),
      });
    }
  });

  it("answers a problem when the reader belongs to no such bookshelf", async () => {
    // Given
    const api = new FetchBookshelvesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.find("9e8d7c6b-5a4f-4e3d-8c2b-1a0f9e8d7c6b");

    // Then
    expect(answer).toEqual({ outcome: "problem", type: "/problems/not-found" });
  });

  it("answers a problem when the id is not one", async () => {
    // Given
    const api = new FetchBookshelvesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.find("not-a-uuid");

    // Then
    expect(answer).toEqual({
      outcome: "problem",
      type: "/problems/validation",
    });
  });

  it("answers a problem when the id holds a slash", async () => {
    // Given
    const api = new FetchBookshelvesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.find("a/b");

    // Then
    expect(answer).toEqual({
      outcome: "problem",
      type: "/problems/validation",
    });
  });

  it("adds the reader with their role", async () => {
    // Given
    const api = new FetchBookshelvesApi(inject("mockBaseUrl"));

    // When
    const answer = await api.addMember(
      "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f",
      "0199c0de-1000-7000-8000-000000000004",
      "OWNER",
    );

    // Then
    expect(answer).toEqual({
      outcome: "added",
      member: {
        reader: { id: expect.any(String), displayName: expect.any(String) },
        role: expect.stringMatching(/^(OWNER|VIEWER)$/),
      },
    });
  });
});
