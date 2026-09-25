import { describe, expect, it } from "vitest";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { lea } from "../fixture/Readers";
import type { AddAnswer } from "./BookshelfApi";
import { useAddBookToBookshelf } from "./useAddBookToBookshelf";

const added: AddAnswer = {
  outcome: "added",
  copy: {
    id: "5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50",
    bookshelf: lea.defaultBookshelf,
  },
};

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
});
