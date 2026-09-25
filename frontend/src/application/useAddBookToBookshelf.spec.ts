import { flushPromises } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { Copy } from "../domain/Copy";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { lea } from "../fixture/Readers";
import { onePiece1 } from "../fixture/SourceEditions";
import type { AddAnswer } from "./BookshelfApi";
import { useAddBookToBookshelf } from "./useAddBookToBookshelf";

const copy: Copy = {
  id: "5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50",
  bookshelf: lea.defaultBookshelf,
};

const added: AddAnswer = { outcome: "added", copy };

describe("useAddBookToBookshelf", () => {
  it("is ready before any add", () => {
    // When
    const { state } = useAddBookToBookshelf(
      new FakeMeApi(lea),
      new FakeBookshelfApi(added),
    );

    // Then
    expect(state.value).toEqual({ status: "ready" });
  });

  it("is adding while the add runs", async () => {
    // Given
    const { state, add } = useAddBookToBookshelf(
      new FakeMeApi(lea),
      new FakeBookshelfApi(new Promise<AddAnswer>(() => {})),
    );

    // When
    void add(onePiece1);
    await flushPromises();

    // Then
    expect(state.value).toEqual({ status: "adding" });
  });

  it("adds the edition to the reader's default bookshelf", async () => {
    // Given
    const bookshelfApi = new FakeBookshelfApi(added);
    const { add } = useAddBookToBookshelf(new FakeMeApi(lea), bookshelfApi);

    // When
    await add(onePiece1);

    // Then
    expect(bookshelfApi.asked).toEqual([
      { bookshelfId: lea.defaultBookshelf.id, edition: onePiece1 },
    ]);
  });

  it("is added with the copy Libris answered", async () => {
    // Given
    const { state, add } = useAddBookToBookshelf(
      new FakeMeApi(lea),
      new FakeBookshelfApi(added),
    );

    // When
    await add(onePiece1);

    // Then
    expect(state.value).toEqual({ status: "added", copy });
  });
});
