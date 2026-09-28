import { describe, expect, it } from "vitest";
import { FakeCatalogueApi } from "../fixture/FakeCatalogueApi";
import { useBrowseCatalogue } from "./useBrowseCatalogue";

describe("useBrowseCatalogue", () => {
  it("is loading until the first page comes", () => {
    // Given
    const { state, browse } = useBrowseCatalogue(
      new FakeCatalogueApi([{ books: [], next: null }]),
    );

    // When
    void browse();

    // Then
    expect(state.value).toEqual({ status: "loading" });
  });
});
