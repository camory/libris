import { describe, expect, it } from "vitest";
import type { Bookshelf } from "../domain/Bookshelf";
import type { KnownReader } from "../domain/KnownReader";
import type { Member } from "../domain/Member";
import { FakeBookshelvesApi } from "../fixture/FakeBookshelvesApi";
import { useAddMemberToBookshelf } from "./useAddMemberToBookshelf";

describe("useAddMemberToBookshelf", () => {
  it("adds the reader with the role chosen", async () => {
    // Given
    const { state, add } = useAddMemberToBookshelf(addingHugoToSalon());
    const before = state.value;

    // When
    await add(salon.id, hugo.id, "OWNER");

    // Then
    expect(before).toEqual({ status: "ready", added: [] });
    expect(state.value).toEqual({ status: "added", added: [owner(hugo)] });
  });

  it("is adding while Libris answers", () => {
    // Given
    const { state, add } = useAddMemberToBookshelf(addingHugoToSalon());

    // When
    void add(salon.id, hugo.id, "OWNER");

    // Then
    expect(state.value).toEqual({ status: "adding", added: [] });
  });

  it("keeps every member it added", async () => {
    // Given
    const { state, add } = useAddMemberToBookshelf(addingHugoToSalon());
    await add(salon.id, hugo.id, "VIEWER");

    // When
    await add(salon.id, hugo.id, "OWNER");

    // Then
    expect(state.value.added).toEqual([viewer(hugo), owner(hugo)]);
  });
});

function addingHugoToSalon() {
  return new FakeBookshelvesApi({
    memberships: [],
    find: {
      outcome: "found",
      bookshelf: salon,
      members: [owner(lea)],
    },
    adds: hugo,
  });
}

const salon: Bookshelf = {
  id: "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f",
  name: "Salon",
};

const lea: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000002",
  displayName: "Léa",
};

const hugo: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000004",
  displayName: "Hugo",
};

function owner(reader: KnownReader): Member {
  return { reader, role: "OWNER" };
}

function viewer(reader: KnownReader): Member {
  return { reader, role: "VIEWER" };
}
