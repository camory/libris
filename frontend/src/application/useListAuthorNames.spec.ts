import { describe, expect, it } from "vitest";
import { FakeAuthorsApi } from "../fixture/FakeAuthorsApi";
import { useListAuthorNames } from "./useListAuthorNames";

describe("useListAuthorNames", () => {
  it("holds the names Libris answers", async () => {
    // Given
    const { names, list } = useListAuthorNames(
      new FakeAuthorsApi(["René Goscinny", "Hergé"]),
    );
    const before = names.value;

    // When
    await list();

    // Then
    expect(before).toBeNull();
    expect(names.value).toEqual(["René Goscinny", "Hergé"]);
  });
});
