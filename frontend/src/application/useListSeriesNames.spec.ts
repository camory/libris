import { describe, expect, it } from "vitest";
import { FakeSeriesApi } from "../fixture/FakeSeriesApi";
import { useListSeriesNames } from "./useListSeriesNames";

describe("useListSeriesNames", () => {
  it("holds the names Libris answers", async () => {
    // Given
    const { names, list } = useListSeriesNames(
      new FakeSeriesApi([], undefined, ["One piece", "Astérix"]),
    );
    const before = names.value;

    // When
    await list();

    // Then
    expect(before).toBeNull();
    expect(names.value).toEqual(["One piece", "Astérix"]);
  });

  it("holds no names when Libris does not answer", async () => {
    // Given
    const { names, list } = useListSeriesNames(
      new FakeSeriesApi([], undefined, new TypeError("Failed to fetch")),
    );

    // When
    const listed = list();

    // Then
    await expect(listed).resolves.toBeUndefined();
    expect(names.value).toBeNull();
  });
});
