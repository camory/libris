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

  it("holds no names when Libris does not answer", async () => {
    // Given
    const { names, list } = useListAuthorNames(
      new FakeAuthorsApi(new TypeError("Failed to fetch")),
    );

    // When
    const listed = list();

    // Then
    await expect(listed).resolves.toBeUndefined();
    expect(names.value).toBeNull();
  });
});
