import { describe, expect, it } from "vitest";
import type { Bookshelf } from "../domain/Bookshelf";
import { FakeBookshelvesApi } from "../fixture/FakeBookshelvesApi";
import { useCreateBookshelf } from "./useCreateBookshelf";

describe("useCreateBookshelf", () => {
  it("answers the bookshelf created, the reader its owner", async () => {
    // Given
    const { state, create } = useCreateBookshelf(
      new FakeBookshelvesApi({
        memberships: [],
        create: { outcome: "created", bookshelf: bibliothequeFamiliale },
      }),
    );
    const before = state.value;

    // When
    await create("Bibliothèque familiale");

    // Then
    expect(before).toEqual({ status: "ready", created: [] });
    expect(state.value).toEqual({
      status: "created",
      created: [{ bookshelf: bibliothequeFamiliale, role: "OWNER" }],
    });
  });

  it("keeps every bookshelf it created", async () => {
    // Given
    const { state, create } = useCreateBookshelf(
      new FakeBookshelvesApi({
        memberships: [],
        create: { outcome: "created", bookshelf: bibliothequeFamiliale },
      }),
    );
    await create("Bibliothèque familiale");

    // When
    await create("Bibliothèque familiale");

    // Then
    expect(state.value.created).toEqual([
      { bookshelf: bibliothequeFamiliale, role: "OWNER" },
      { bookshelf: bibliothequeFamiliale, role: "OWNER" },
    ]);
  });

  it("says the name is taken when Libris refuses it", async () => {
    // Given
    const { state, create } = useCreateBookshelf(
      new FakeBookshelvesApi({
        memberships: [],
        create: { outcome: "problem", type: "/problems/conflict" },
      }),
    );

    // When
    await create("Salon");

    // Then
    expect(state.value).toEqual({ status: "taken", created: [] });
  });

  it("is unavailable when Libris does not answer", async () => {
    // Given
    const { state, create } = useCreateBookshelf(
      new FakeBookshelvesApi({
        memberships: [],
        create: new TypeError("Failed to fetch"),
      }),
    );

    // When
    await create("Bibliothèque familiale");

    // Then
    expect(state.value).toEqual({ status: "unavailable", created: [] });
  });

  it("is creating until Libris answers", () => {
    // Given
    const { state, create } = useCreateBookshelf(
      new FakeBookshelvesApi({
        memberships: [],
        create: { outcome: "created", bookshelf: bibliothequeFamiliale },
      }),
    );

    // When
    void create("Bibliothèque familiale");

    // Then
    expect(state.value).toEqual({ status: "creating", created: [] });
  });
});

const bibliothequeFamiliale: Bookshelf = {
  id: "4f5a6b7c-8d9e-4f0a-9b2c-3d4e5f6a7b82",
  name: "Bibliothèque familiale",
};
