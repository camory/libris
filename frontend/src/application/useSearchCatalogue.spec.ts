import { describe, expect, it } from "vitest";
import { asterixLeGaulois, lePetitPrince, romanceDawn } from "../fixture/Books";
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
      loadingMore: false,
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
      loadingMore: false,
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

  it("holds the books shown, loading more, while the next page comes", async () => {
    // Given
    const { state, search, searchMore } = useSearchCatalogue(
      new FakeSearchApi({
        goscinny: [firstPage(), new Promise(() => {})],
      }),
    );
    await search("goscinny");

    // When
    void searchMore();

    // Then
    expect(state.value).toEqual({
      status: "found",
      books: [romanceDawn],
      loadingMore: true,
    });
  });

  it("holds the nearest of both pages, still said near", async () => {
    // Given
    const { state, search, searchMore } = useSearchCatalogue(
      new FakeSearchApi({ asterik: [firstPage(true), lastPage(true)] }),
    );
    await search("asterik");

    // When
    await searchMore();

    // Then
    expect(state.value).toEqual({
      status: "near",
      query: "asterik",
      books: [romanceDawn, asterixLeGaulois],
      loadingMore: false,
    });
  });

  it("holds the nearest still said near when the next page holds no book", async () => {
    // Given
    const { state, search, searchMore } = useSearchCatalogue(
      new FakeSearchApi({
        asterik: [firstPage(true), { books: [], next: null, near: false }],
      }),
    );
    await search("asterik");

    // When
    await searchMore();

    // Then
    expect(state.value).toEqual({
      status: "near",
      query: "asterik",
      books: [romanceDawn],
      loadingMore: false,
    });
  });

  it("asks nothing more after a page naming no next", async () => {
    // Given
    const searchApi = new FakeSearchApi({ goscinny: [lastPage()] });
    const { search, searchMore } = useSearchCatalogue(searchApi);
    await search("goscinny");

    // When
    await searchMore();

    // Then
    expect(searchApi.asked).toEqual([{ query: "goscinny", after: null }]);
  });

  it("asks nothing while the next page comes", async () => {
    // Given
    const searchApi = new FakeSearchApi({
      goscinny: [firstPage(), new Promise(() => {})],
    });
    const { search, searchMore } = useSearchCatalogue(searchApi);
    await search("goscinny");
    void searchMore();

    // When
    await searchMore();

    // Then
    expect(searchApi.asked).toHaveLength(2);
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
      loadingMore: false,
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
    expect(state.value).toEqual({
      status: "found",
      books: [romanceDawn],
      loadingMore: false,
    });
  });

  it("holds Libris unavailable, no book, when the first page does not come", async () => {
    // Given
    const { state, search } = useSearchCatalogue(
      new FakeSearchApi({ goscinny: [new TypeError("Failed to fetch")] }),
    );

    // When
    await search("goscinny");

    // Then
    expect(state.value).toEqual({
      status: "unavailable",
      query: "goscinny",
      books: [],
      near: false,
    });
  });

  it("lists no book of the search shown when the next search does not come", async () => {
    // Given
    const { state, search } = useSearchCatalogue(
      new FakeSearchApi({
        goscinny: [lastPage()],
        oda: [new TypeError("Failed to fetch")],
      }),
    );
    await search("goscinny");

    // When
    await search("oda");

    // Then
    expect(state.value).toEqual({
      status: "unavailable",
      query: "oda",
      books: [],
      near: false,
    });
  });

  it("holds the latest search's answer when an earlier one arrives after it", async () => {
    // Given
    const goscinny = later();
    const oda = later();
    const { state, search } = useSearchCatalogue(
      new FakeSearchApi({ goscinny: [goscinny.page], oda: [oda.page] }),
    );
    const earlier = search("goscinny");
    const latest = search("oda");

    // When
    oda.arrives({ books: [romanceDawn], next: null, near: false });
    await latest;
    goscinny.arrives({ books: [asterixLeGaulois], next: null, near: false });
    await earlier;

    // Then
    expect(state.value).toEqual({
      status: "found",
      books: [romanceDawn],
      loadingMore: false,
    });
  });

  it("asks the latest search's next page after an earlier answer arrives late", async () => {
    // Given
    const goscinny = later();
    const oda = later();
    const searchApi = new FakeSearchApi({
      goscinny: [goscinny.page],
      oda: [oda.page, lastPage()],
    });
    const { search, searchMore } = useSearchCatalogue(searchApi);
    const earlier = search("goscinny");
    const latest = search("oda");
    oda.arrives(firstPage());
    await latest;
    goscinny.arrives({
      books: [asterixLeGaulois],
      next: asterixLeGaulois.id,
      near: false,
    });
    await earlier;

    // When
    await searchMore();

    // Then
    expect(searchApi.asked.at(-1)).toEqual({
      query: "oda",
      after: romanceDawn.id,
    });
  });

  it("drops a next page of an earlier search arriving after the latest search's answer", async () => {
    // Given
    const nextPage = later();
    const { state, search, searchMore } = useSearchCatalogue(
      new FakeSearchApi({
        goscinny: [firstPage(), nextPage.page],
        oda: [{ books: [lePetitPrince], next: null, near: false }],
      }),
    );
    await search("goscinny");
    const more = searchMore();
    await search("oda");

    // When
    nextPage.arrives(lastPage());
    await more;

    // Then
    expect(state.value).toEqual({
      status: "found",
      books: [lePetitPrince],
      loadingMore: false,
    });
  });

  it("drops a next page asked while a later search is coming", async () => {
    // Given
    const nextPage = later();
    const oda = later();
    const { state, search, searchMore } = useSearchCatalogue(
      new FakeSearchApi({
        goscinny: [firstPage(), nextPage.page],
        oda: [oda.page],
      }),
    );
    await search("goscinny");
    const latest = search("oda");
    const more = searchMore();

    // When
    oda.arrives({ books: [lePetitPrince], next: null, near: false });
    await latest;
    nextPage.arrives(lastPage());
    await more;

    // Then
    expect(state.value).toEqual({
      status: "found",
      books: [lePetitPrince],
      loadingMore: false,
    });
  });

  it("drops an earlier answer to the same query asked again", async () => {
    // Given
    const first = later();
    const second = later();
    const { state, search } = useSearchCatalogue(
      new FakeSearchApi({ goscinny: [first.page, second.page] }),
    );
    const earlier = search("goscinny");
    const latest = search("goscinny");

    // When
    second.arrives({ books: [romanceDawn], next: null, near: false });
    await latest;
    first.arrives({ books: [asterixLeGaulois], next: null, near: false });
    await earlier;

    // Then
    expect(state.value).toEqual({
      status: "found",
      books: [romanceDawn],
      loadingMore: false,
    });
  });

  it("shows the whole catalogue when a search arrives after the box is emptied", async () => {
    // Given
    const goscinny = later();
    const { state, search } = useSearchCatalogue(
      new FakeSearchApi({ goscinny: [goscinny.page] }),
    );
    const earlier = search("goscinny");
    await search("");

    // When
    goscinny.arrives({ books: [romanceDawn], next: null, near: false });
    await earlier;

    // Then
    expect(state.value).toEqual({ status: "ready" });
  });

  function firstPage(near = false): SearchPage {
    return { books: [romanceDawn], next: romanceDawn.id, near };
  }

  function lastPage(near = false): SearchPage {
    return { books: [asterixLeGaulois], next: null, near };
  }

  function later() {
    let arrives: (page: SearchPage) => void = () => {};
    const page = new Promise<SearchPage>((resolve) => {
      arrives = resolve;
    });
    return { page, arrives };
  }
});
