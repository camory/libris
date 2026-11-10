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

  it("is unavailable when Libris does not answer", async () => {
    // Given
    const { state, find } = useFindCatalogueEdition(
      new FakeCatalogueApi([], new TypeError("Failed to fetch")),
    );

    // When
    await find(asterixLeGaulois.id);

    // Then
    expect(state.value).toEqual({ status: "unavailable" });
  });

  it("keeps the ouvrage found on screen while it is asked again", async () => {
    // Given
    const fewerCopies = {
      ...asterixLeGaulois,
      copies: asterixLeGaulois.copies.slice(1),
    };
    const { state, find, refresh } = useFindCatalogueEdition(
      new FakeCatalogueApi(
        [],
        [
          { outcome: "found", book: asterixLeGaulois },
          { outcome: "found", book: fewerCopies },
        ],
      ),
    );
    await find(asterixLeGaulois.id);

    // When
    const asking = refresh(asterixLeGaulois.id);
    const during = state.value;
    await asking;

    // Then
    expect([during, state.value]).toEqual([
      { status: "found", book: asterixLeGaulois },
      { status: "found", book: fewerCopies },
    ]);
  });

  it("reads another problem as no answer", async () => {
    // Given
    const { state, find } = useFindCatalogueEdition(
      new FakeCatalogueApi([], {
        outcome: "problem",
        type: "/problems/conflict",
      }),
    );

    // When
    await find(asterixLeGaulois.id);

    // Then
    expect(state.value).toEqual({ status: "unavailable" });
  });

  it("shows the ouvrage it is given, asking nothing", () => {
    // Given
    const { state, show } = useFindCatalogueEdition(new FakeCatalogueApi([]));

    // When
    show(asterixLeGaulois);

    // Then
    expect(state.value).toEqual({ status: "found", book: asterixLeGaulois });
  });
});
