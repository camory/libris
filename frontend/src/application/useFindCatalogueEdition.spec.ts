import { describe, expect, it } from "vitest";
import { asterixLeGaulois } from "../fixture/Books";
import { FakeCatalogueApi } from "../fixture/FakeCatalogueApi";
import { useFindCatalogueEdition } from "./useFindCatalogueEdition";

describe("useFindCatalogueEdition", () => {
  it("answers the ouvrage Libris found", async () => {
    // Given
    const { state, find } = useFindCatalogueEdition(
      new FakeCatalogueApi([], { outcome: "found", book: asterixLeGaulois }),
    );

    // When
    await find(asterixLeGaulois.id);

    // Then
    expect(state.value).toEqual({ status: "found", book: asterixLeGaulois });
  });

  it("says there is no such ouvrage when Libris answers none", async () => {
    // Given
    const { state, find } = useFindCatalogueEdition(
      new FakeCatalogueApi([], {
        outcome: "problem",
        type: "/problems/not-found",
      }),
    );
    const before = state.value;

    // When
    await find(asterixLeGaulois.id);

    // Then
    expect([before, state.value]).toEqual([
      { status: "loading" },
      { status: "notFound" },
    ]);
  });

  it("says there is no such ouvrage when the id is not one", async () => {
    // Given
    const { state, find } = useFindCatalogueEdition(
      new FakeCatalogueApi([], {
        outcome: "problem",
        type: "/problems/validation",
      }),
    );

    // When
    await find("asterix");

    // Then
    expect(state.value).toEqual({ status: "notFound" });
  });
});
