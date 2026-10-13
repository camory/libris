import { describe, expect, it } from "vitest";
import type { Membership } from "../domain/Membership";
import { FakeBookshelvesApi } from "../fixture/FakeBookshelvesApi";
import { chloe } from "../fixture/Readers";
import { useListReaderBookshelves } from "./useListReaderBookshelves";

describe("useListReaderBookshelves", () => {
  it("lists the reader's memberships", async () => {
    // Given
    const { state, list } = useListReaderBookshelves(
      new FakeBookshelvesApi({ memberships: [viewerOfSalon, ownerOfDefault] }),
    );
    const before = state.value;

    // When
    await list();

    // Then
    expect(before).toEqual({ status: "loading" });
    expect(state.value).toEqual({
      status: "listed",
      memberships: [viewerOfSalon, ownerOfDefault],
    });
  });
});

const viewerOfSalon: Membership = {
  bookshelf: { id: "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f", name: "Salon" },
  role: "VIEWER",
};

const ownerOfDefault: Membership = {
  bookshelf: chloe.defaultBookshelf,
  role: "OWNER",
};
