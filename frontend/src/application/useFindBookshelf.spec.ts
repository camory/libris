import { describe, expect, it } from "vitest";
import type { FindAnswer } from "./BookshelvesApi";
import type { Bookshelf } from "../domain/Bookshelf";
import type { KnownReader } from "../domain/KnownReader";
import type { Member } from "../domain/Member";
import { FakeBookshelvesApi } from "../fixture/FakeBookshelvesApi";
import { FakeReadersApi } from "../fixture/FakeReadersApi";
import { useFindBookshelf } from "./useFindBookshelf";

describe("useFindBookshelf", () => {
  it("finds the bookshelf and its members", async () => {
    // Given
    const { state, find } = useFindBookshelf(
      new FakeBookshelvesApi({
        memberships: [],
        find: { outcome: "found", bookshelf: salon, members: [lea, marc] },
      }),
      new FakeReadersApi([]),
    );
    const before = state.value;

    // When
    await find(salon.id, lea.reader.id);

    // Then
    expect(before).toEqual({ status: "loading" });
    expect(state.value).toEqual({
      status: "found",
      bookshelf: salon,
      members: [lea, marc],
      readers: [],
    });
  });

  it("reads another problem as no answer", async () => {
    // Given
    const { state, find } = findAnswering({
      outcome: "problem",
      type: "/problems/conflict",
    });

    // When
    await find(salon.id, lea.reader.id);

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
    await find(salon.id, lea.reader.id);

    // Then
    expect(state.value).toEqual({ status: "notFound" });
  });

  it("reads an id that is not one as no such bookshelf", async () => {
    // Given
    const { state, find } = findAnswering({
      outcome: "problem",
      type: "/problems/validation",
    });

    // When
    await find("not-a-uuid", lea.reader.id);

    // Then
    expect(state.value).toEqual({ status: "notFound" });
  });

  it("is unavailable when Libris does not answer", async () => {
    // Given
    const { state, find } = findAnswering(new TypeError("Failed to fetch"));

    // When
    await find(salon.id, lea.reader.id);

    // Then
    expect(state.value).toEqual({ status: "unavailable" });
  });

  it("is unavailable when the readers do not answer", async () => {
    // Given
    const { state, find } = useFindBookshelf(
      new FakeBookshelvesApi({
        memberships: [],
        find: {
          outcome: "found",
          bookshelf: salon,
          members: [owner(leaToOthers), viewer(marcToOthers)],
        },
      }),
      new FakeReadersApi(new TypeError("Failed to fetch")),
    );

    // When
    await find(salon.id, leaToOthers.id);

    // Then
    expect(state.value).toEqual({ status: "unavailable" });
  });

  it("asks the readers Libris knows for an owner", async () => {
    // Given
    const { state, find } = useFindBookshelf(
      new FakeBookshelvesApi({
        memberships: [],
        find: {
          outcome: "found",
          bookshelf: salon,
          members: [owner(leaToOthers), viewer(marcToOthers)],
        },
      }),
      new FakeReadersApi([leaToOthers, marcToOthers, hugo]),
    );

    // When
    await find(salon.id, leaToOthers.id);

    // Then
    expect(state.value).toEqual({
      status: "found",
      bookshelf: salon,
      members: [owner(leaToOthers), viewer(marcToOthers)],
      readers: [leaToOthers, marcToOthers, hugo],
    });
  });

  it("asks no reader for a viewer", async () => {
    // Given
    const { state, find } = useFindBookshelf(
      new FakeBookshelvesApi({
        memberships: [],
        find: {
          outcome: "found",
          bookshelf: salon,
          members: [viewer(leaToOthers), owner(marcToOthers)],
        },
      }),
      new FakeReadersApi([leaToOthers, marcToOthers, hugo]),
    );

    // When
    await find(salon.id, leaToOthers.id);

    // Then
    expect(state.value).toEqual({
      status: "found",
      bookshelf: salon,
      members: [viewer(leaToOthers), owner(marcToOthers)],
      readers: [],
    });
  });
});

function findAnswering(find: FindAnswer | Error) {
  return useFindBookshelf(
    new FakeBookshelvesApi({ memberships: [], find }),
    new FakeReadersApi([]),
  );
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

const leaToOthers: KnownReader = lea.reader;

const marcToOthers: KnownReader = marc.reader;

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
