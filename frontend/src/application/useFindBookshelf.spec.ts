import { describe, expect, it } from "vitest";
import type { FindAnswer } from "./BookshelvesApi";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Member } from "../domain/Member";
import { FakeBookshelvesApi } from "../fixture/FakeBookshelvesApi";
import { useFindBookshelf } from "./useFindBookshelf";

describe("useFindBookshelf", () => {
  it("finds the bookshelf and its members", async () => {
    // Given
    const { state, find } = useFindBookshelf(
      new FakeBookshelvesApi({
        memberships: [],
        find: { outcome: "found", bookshelf: salon, members: [lea, marc] },
      }),
    );
    const before = state.value;

    // When
    await find(salon.id);

    // Then
    expect(before).toEqual({ status: "loading" });
    expect(state.value).toEqual({
      status: "found",
      bookshelf: salon,
      members: [lea, marc],
    });
  });

  it("reads another problem as no answer", async () => {
    // Given
    const { state, find } = findAnswering({
      outcome: "problem",
      type: "/problems/conflict",
    });

    // When
    await find(salon.id);

    // Then
    expect(state.value).toEqual({ status: "unavailable" });
  });

  it("says there is no such bookshelf", async () => {
    // Given
    const { state, find } = findAnswering({
      outcome: "problem",
      type: "/problems/not-found",
    });

    // When
    await find(salon.id);

    // Then
    expect(state.value).toEqual({ status: "notFound" });
  });
});

function findAnswering(find: FindAnswer | Error) {
  return useFindBookshelf(new FakeBookshelvesApi({ memberships: [], find }));
}

const salon: Bookshelf = {
  id: "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f",
  name: "Salon",
};

const lea: Member = {
  reader: { id: "0199c0de-1000-7000-8000-000000000002", displayName: "Léa" },
  role: "VIEWER",
};

const marc: Member = {
  reader: { id: "0199c0de-1000-7000-8000-000000000005", displayName: "Marc" },
  role: "OWNER",
};
