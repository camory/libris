import { describe, expect, it } from "vitest";
import type { Series } from "../domain/Series";
import { FakeSeriesApi } from "../fixture/FakeSeriesApi";
import { useListCatalogueSeries } from "./useListCatalogueSeries";

describe("useListCatalogueSeries", () => {
  it("says none when Libris answers no series", async () => {
    // Given
    const { state, list } = useListCatalogueSeries(new FakeSeriesApi([]));
    const before = state.value;

    // When
    await list();

    // Then
    expect(before).toEqual({ status: "loading" });
    expect(state.value).toEqual({ status: "empty" });
  });

  it("lists the series Libris answers", async () => {
    // Given
    const { state, list } = useListCatalogueSeries(
      new FakeSeriesApi([onePiece]),
    );

    // When
    await list();

    // Then
    expect(state.value).toEqual({ status: "listed", series: [onePiece] });
  });

  it("is unavailable when Libris does not answer", async () => {
    // Given
    const { state, list } = useListCatalogueSeries(
      new FakeSeriesApi(new TypeError("Failed to fetch")),
    );

    // When
    const listed = list();

    // Then
    await expect(listed).resolves.toBeUndefined();
    expect(state.value).toEqual({ status: "unavailable" });
  });
});

const onePiece: Series = {
  name: "One piece",
  volumeCount: 3,
  coverUrl: null,
  kind: "MANGA",
};
