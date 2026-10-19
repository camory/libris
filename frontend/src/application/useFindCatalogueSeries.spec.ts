import { describe, expect, it } from "vitest";
import { FakeSeriesApi } from "../fixture/FakeSeriesApi";
import { useFindCatalogueSeries } from "./useFindCatalogueSeries";

describe("useFindCatalogueSeries", () => {
  it("says there is no such series when Libris answers none", async () => {
    // Given
    const { state, find } = useFindCatalogueSeries(
      new FakeSeriesApi([], {
        outcome: "problem",
        type: "/problems/not-found",
      }),
    );
    const before = state.value;

    // When
    await find(asterixId);

    // Then
    expect([before, state.value]).toEqual([
      { status: "loading" },
      { status: "notFound" },
    ]);
  });
});

const asterixId = "4f60718a-b2c3-4d4e-8f60-718293a4b5c6";
