import { describe, expect, it } from "vitest";
import {
  asterixEtSesAmis,
  asterixLeGaulois,
  romanceDawn,
} from "../fixture/Books";
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

  it("asks for the first page", async () => {
    // Given
    const catalogueApi = new FakeCatalogueApi([{ books: [], next: null }]);
    const { browse } = useBrowseCatalogue(catalogueApi);

    // When
    await browse();

    // Then
    expect(catalogueApi.asked).toEqual([null]);
  });

  it("is empty when the first page holds no book", async () => {
    // Given
    const { state, browse } = useBrowseCatalogue(
      new FakeCatalogueApi([{ books: [], next: null }]),
    );

    // When
    await browse();

    // Then
    expect(state.value).toEqual({ status: "empty" });
  });

  it("lists the books of the first page", async () => {
    // Given
    const { state, browse } = useBrowseCatalogue(
      new FakeCatalogueApi([
        { books: [asterixLeGaulois, romanceDawn], next: null },
      ]),
    );

    // When
    await browse();

    // Then
    expect(state.value).toEqual({
      status: "listed",
      books: [asterixLeGaulois, romanceDawn],
    });
  });
  it("is unavailable when the first page does not come", async () => {
    // Given
    const { state, browse } = useBrowseCatalogue(
      new FakeCatalogueApi([new TypeError("Failed to fetch")]),
    );

    // When
    await browse();

    // Then
    expect(state.value).toEqual({ status: "unavailable" });
  });
  it("is unavailable when the catalogue refuses the page", async () => {
    // Given
    const { state, browse } = useBrowseCatalogue(
      new FakeCatalogueApi([Error("The catalogue answered 503")]),
    );

    // When
    await browse();

    // Then
    expect(state.value).toEqual({ status: "unavailable" });
  });

  it("lists the next page after the first", async () => {
    // Given
    const { state, browse } = useBrowseCatalogue(
      new FakeCatalogueApi([firstPage(), lastPage()]),
    );
    await browse();

    // When
    await browse();

    // Then
    expect(state.value).toEqual({
      status: "listed",
      books: [asterixLeGaulois, asterixEtSesAmis, romanceDawn],
    });
  });

  it("asks the next page with the next of the page received", async () => {
    // Given
    const catalogueApi = new FakeCatalogueApi([firstPage(), lastPage()]);
    const { browse } = useBrowseCatalogue(catalogueApi);
    await browse();

    // When
    await browse();

    // Then
    expect(catalogueApi.asked).toEqual([null, asterixEtSesAmis.id]);
  });

  it("asks no page after the last", async () => {
    // Given
    const catalogueApi = new FakeCatalogueApi([firstPage(), lastPage()]);
    const { state, browse } = useBrowseCatalogue(catalogueApi);
    await browse();
    await browse();

    // When
    await browse();

    // Then
    expect(catalogueApi.asked).toEqual([null, asterixEtSesAmis.id]);
    expect(state.value).toEqual({
      status: "listed",
      books: [asterixLeGaulois, asterixEtSesAmis, romanceDawn],
    });
  });

  it("is loading more while the next page is coming", async () => {
    // Given
    const { state, browse } = useBrowseCatalogue(
      new FakeCatalogueApi([firstPage(), lastPage()]),
    );
    await browse();

    // When
    void browse();

    // Then
    expect(state.value).toEqual({
      status: "loadingMore",
      books: [asterixLeGaulois, asterixEtSesAmis],
    });
  });

  it("asks the next page once while it is on its way", async () => {
    // Given
    const catalogueApi = new FakeCatalogueApi([firstPage(), lastPage()]);
    const { browse } = useBrowseCatalogue(catalogueApi);
    await browse();
    void browse();

    // When
    await browse();

    // Then
    expect(catalogueApi.asked).toEqual([null, asterixEtSesAmis.id]);
  });

  function firstPage() {
    return {
      books: [asterixLeGaulois, asterixEtSesAmis],
      next: asterixEtSesAmis.id,
    };
  }

  function lastPage() {
    return { books: [romanceDawn], next: null };
  }
});
