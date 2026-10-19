import { describe, expect, it } from "vitest";
import type { SeriesWithBooks } from "../domain/Series";
import { asterixEtSesAmis, asterixLeGaulois } from "../fixture/Books";
import { FakeSeriesApi } from "../fixture/FakeSeriesApi";
import { useFindCatalogueSeries } from "./useFindCatalogueSeries";

describe("useFindCatalogueSeries", () => {
  it("answers the series Libris finds", async () => {
    // Given
    const { state, find } = useFindCatalogueSeries(
      new FakeSeriesApi([], { outcome: "found", series: asterix }),
    );

    // When
    await find(asterix.id);

    // Then
    expect(state.value).toEqual({ status: "found", series: asterix });
  });

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

  it("says there is no such series when the id is not one", async () => {
    // Given
    const { state, find } = useFindCatalogueSeries(
      new FakeSeriesApi([], {
        outcome: "problem",
        type: "/problems/validation",
      }),
    );

    // When
    await find("tintin");

    // Then
    expect(state.value).toEqual({ status: "notFound" });
  });
});

const asterixId = "4f60718a-b2c3-4d4e-8f60-718293a4b5c6";

const asterix: SeriesWithBooks = {
  id: asterixId,
  name: "Astérix",
  volumeCount: 2,
  coverUrl: null,
  kind: "BD",
  books: [asterixEtSesAmis, asterixLeGaulois],
};
