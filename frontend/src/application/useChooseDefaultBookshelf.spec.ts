import { describe, expect, it } from "vitest";
import type { ChooseDefaultAnswer } from "./BookshelvesApi";
import type { Bookshelf } from "../domain/Bookshelf";
import { bibliothequeDeLea, salon } from "../fixture/Books";
import { FakeBookshelvesApi } from "../fixture/FakeBookshelvesApi";
import { useChooseDefaultBookshelf } from "./useChooseDefaultBookshelf";

describe("useChooseDefaultBookshelf", () => {
  it("makes the bookshelf the default", async () => {
    // Given
    const { state, choose } = useChooseDefaultBookshelf(librisChoosing());
    const before = state.value;

    // When
    await choose(salon.id);

    // Then
    expect(before).toEqual({
      status: "ready",
      bookshelfId: null,
      defaultBookshelfId: null,
    });
    expect(state.value).toEqual({
      status: "chosen",
      bookshelfId: salon.id,
      defaultBookshelfId: salon.id,
    });
  });

  it("is choosing while Libris answers", () => {
    // Given
    const { state, choose } = useChooseDefaultBookshelf(
      new LibrisNotAnswering(),
    );

    // When
    void choose(salon.id);

    // Then
    expect(state.value).toEqual({
      status: "choosing",
      bookshelfId: salon.id,
      defaultBookshelfId: null,
    });
  });

  it("reads a refusal as no answer", async () => {
    // Given
    const { state, choose } = useChooseDefaultBookshelf(librisChoosing());

    // When
    await choose(mangas.id);

    // Then
    expect(state.value).toEqual({
      status: "unavailable",
      bookshelfId: mangas.id,
      defaultBookshelfId: null,
    });
  });

  it("is unavailable when Libris does not answer", async () => {
    // Given
    const { state, choose } = useChooseDefaultBookshelf(
      new FakeBookshelvesApi({
        memberships: [{ bookshelf: salon, role: "OWNER" }],
        defaultChosen: new TypeError("Failed to fetch"),
      }),
    );

    // When
    await choose(salon.id);

    // Then
    expect(state.value).toEqual({
      status: "unavailable",
      bookshelfId: salon.id,
      defaultBookshelfId: null,
    });
  });

  function librisChoosing() {
    return new FakeBookshelvesApi({
      memberships: [
        { bookshelf: bibliothequeDeLea, role: "OWNER" },
        { bookshelf: salon, role: "OWNER" },
        { bookshelf: mangas, role: "VIEWER" },
      ],
      defaultChosen: () => {},
    });
  }
});

const mangas: Bookshelf = {
  id: "3e4f5a6b-7c8d-4e9f-8a1b-2c3d4e5f6a71",
  name: "Mangas",
};

class LibrisNotAnswering extends FakeBookshelvesApi {
  constructor() {
    super({ memberships: [] });
  }

  override chooseDefault(): Promise<ChooseDefaultAnswer> {
    return new Promise(() => {});
  }
}
