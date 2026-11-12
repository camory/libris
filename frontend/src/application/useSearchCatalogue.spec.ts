import { describe, expect, it } from "vitest";
import { asterixLeGaulois, romanceDawn } from "../fixture/Books";
import { FakeSearchApi } from "../fixture/FakeSearchApi";
import type { SearchPage } from "./SearchApi";
import { useSearchCatalogue } from "./useSearchCatalogue";

describe("useSearchCatalogue", () => {
  it("holds the books of the first page", async () => {
    // Given
    const { state, search } = useSearchCatalogue(
      new FakeSearchApi({
        goscinny: [
          { books: [romanceDawn, asterixLeGaulois], next: null, near: false },
        ],
      }),
    );

    // When
    await search("goscinny");

    // Then
    expect(state.value).toEqual({
      status: "found",
      books: [romanceDawn, asterixLeGaulois],
    });
  });

  it("holds the next page's books after the first's", async () => {
    // Given
    const { state, search, searchMore } = useSearchCatalogue(
      new FakeSearchApi({ goscinny: [firstPage(), lastPage()] }),
    );
    await search("goscinny");

    // When
    await searchMore();

    // Then
    expect(state.value).toEqual({
      status: "found",
      books: [romanceDawn, asterixLeGaulois],
    });
  });

  it("asks the next page of the same query after the last page's next", async () => {
    // Given
    const searchApi = new FakeSearchApi({
      goscinny: [firstPage(), lastPage()],
    });
    const { search, searchMore } = useSearchCatalogue(searchApi);
    await search("goscinny");

    // When
    await searchMore();

    // Then
    expect(searchApi.asked).toEqual([
      { query: "goscinny", after: null },
      { query: "goscinny", after: romanceDawn.id },
    ]);
  });

  it("holds no match, with the query asked, when the answer holds no edition", async () => {
    // Given
    const { state, search } = useSearchCatalogue(
      new FakeSearchApi({
        gaston: [{ books: [], next: null, near: false }],
      }),
    );

    // When
    await search(" gaston ");

    // Then
    expect(state.value).toEqual({ status: "nothing", query: "gaston" });
  });

  it("holds the nearest, with the query asked, when the answer is said near", async () => {
    // Given
    const { state, search } = useSearchCatalogue(
      new FakeSearchApi({
        asterik: [{ books: [asterixLeGaulois], next: null, near: true }],
      }),
    );

    // When
    await search("asterik");

    // Then
    expect(state.value).toEqual({
      status: "near",
      query: "asterik",
      books: [asterixLeGaulois],
    });
  });

  it("asks the words without the spaces at their ends", async () => {
    // Given
    const searchApi = new FakeSearchApi({
      goscinny: [{ books: [], next: null, near: false }],
    });
    const { search } = useSearchCatalogue(searchApi);

    // When
    await search(" goscinny ");

    // Then
    expect(searchApi.asked).toEqual([{ query: "goscinny", after: null }]);
  });

  it("asks nothing under two characters", async () => {
    // Given
    const searchApi = new FakeSearchApi();
    const { search } = useSearchCatalogue(searchApi);

    // When
    await search(" g ");

    // Then
    expect(searchApi.asked).toEqual([]);
  });

  it("shows the whole catalogue again under two characters", async () => {
    // Given
    const { state, search } = useSearchCatalogue(
      new FakeSearchApi({
        goscinny: [{ books: [romanceDawn], next: null, near: false }],
      }),
    );
    await search("goscinny");

    // When
    await search("g");

    // Then
    expect(state.value).toEqual({ status: "ready" });
  });

  it("keeps the books found while the next search is answered", async () => {
    // Given
    const { state, search } = useSearchCatalogue(
      new FakeSearchApi({
        goscinny: [{ books: [romanceDawn], next: null, near: false }],
        oda: [new Promise(() => {})],
      }),
    );
    await search("goscinny");

    // When
    void search("oda");

    // Then
    expect(state.value).toEqual({ status: "found", books: [romanceDawn] });
  });

  function firstPage(near = false): SearchPage {
    return { books: [romanceDawn], next: romanceDawn.id, near };
  }

  function lastPage(near = false): SearchPage {
    return { books: [asterixLeGaulois], next: null, near };
  }
});
