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
    expect(before).toEqual({ status: "ready" });
    expect(state.value).toEqual({
      status: "created",
      membership: { bookshelf: bibliothequeFamiliale, role: "OWNER" },
    });
  });
});

const bibliothequeFamiliale: Bookshelf = {
  id: "4f5a6b7c-8d9e-4f0a-9b2c-3d4e5f6a7b82",
  name: "Bibliothèque familiale",
};
