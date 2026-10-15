import { describe, expect, it } from "vitest";
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
});
