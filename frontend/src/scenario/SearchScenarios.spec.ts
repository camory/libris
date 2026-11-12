import {
  fireEvent,
  queries,
  waitFor,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { App } from "vue";
import type { CatalogueApi } from "../application/CatalogueApi";
import {
  searchApiKey,
  type SearchApi,
  type SearchPage,
} from "../application/SearchApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Book } from "../domain/Book";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Copy } from "../domain/Copy";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import { FakeAuthorsApi } from "../fixture/FakeAuthorsApi";
import { FakeBarcodeScanner } from "../fixture/FakeBarcodeScanner";
import { FakeBeepSetting } from "../fixture/FakeBeepSetting";
import { FakeBeeper } from "../fixture/FakeBeeper";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeBookshelvesApi } from "../fixture/FakeBookshelvesApi";
import { FakeCatalogueApi } from "../fixture/FakeCatalogueApi";
import { FakeCopiesApi } from "../fixture/FakeCopiesApi";
import { FakeEditionsApi } from "../fixture/FakeEditionsApi";
import { FakeIsbnApi } from "../fixture/FakeIsbnApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { FakeReadersApi } from "../fixture/FakeReadersApi";
import { FakeSearchApi } from "../fixture/FakeSearchApi";
import { FakeSeriesApi } from "../fixture/FakeSeriesApi";
import { lea } from "../fixture/Readers";

type Screen = BoundFunctions<typeof queries>;

const bibliothequeDeLea: Bookshelf = {
  id: "0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4e",
  name: "Bibliothèque de Léa",
};

describe("Search", () => {
  const host = document.createElement("div");
  let app: App;

  beforeEach(() => {
    document.body.append(host);
  });

  afterEach(() => {
    vi.useRealTimers();
    app.unmount();
    host.remove();
    window.history.replaceState(null, "", "/");
    vi.unstubAllGlobals();
  });

  it("S1 Words find the editions holding them", async () => {
    // Given
    const search = librisAnswers({
      goscinny: [found(asterixLeGaulois, laSerpeDOr)],
    });
    const screen = await theCatalogueOpen(search);

    // When
    await type(screen, " goscinny ");

    // Then
    await waitFor(() =>
      expect(rows(screen)).toEqual([
        reads(asterixLeGaulois),
        reads(laSerpeDOr),
      ]),
    );
    expect(search.asked).toEqual([{ query: "goscinny", after: null }]);
  });

  it.skip("S3 Near matches when the words find nothing", async () => {
    // Given
    const search = librisAnswers({
      asterik: [near(asterixLeGaulois, laSerpeDOr)],
    });
    const screen = await theCatalogueOpen(search);

    // When
    await type(screen, "asterik");

    // Then
    await screen.findByText("Aucun ouvrage ne correspond à « asterik ».");
    screen.getByText("Les plus proches :");
    expect(rows(screen)).toEqual([reads(asterixLeGaulois), reads(laSerpeDOr)]);
  });

  it.skip("S3 Near matches when the words find nothing, none near either", async () => {
    // Given
    const search = librisAnswers({ gaston: [nothing()] });
    const screen = await theCatalogueOpen(search);

    // When
    await type(screen, "gaston");

    // Then
    await screen.findByText("Aucun ouvrage ne correspond à « gaston ».");
    expect(screen.queryByText("Les plus proches :")).toBeNull();
    expect(rows(screen)).toEqual([]);
  });

  it.skip("S4 Results come in pages", async () => {
    // Given
    const search = librisAnswers({
      goscinny: [
        { books: [asterixLeGaulois], next: asterixLeGaulois.id, near: false },
        { books: [laSerpeDOr], next: null, near: false },
      ],
    });
    const screen = await theCatalogueOpen(search);
    await type(screen, "goscinny");
    await waitFor(() =>
      expect(rows(screen)).toEqual([reads(asterixLeGaulois)]),
    );

    // When
    theLastRowComesIntoView();

    // Then
    await waitFor(() =>
      expect(rows(screen)).toEqual([
        reads(asterixLeGaulois),
        reads(laSerpeDOr),
      ]),
    );
    expect(search.asked).toEqual([
      { query: "goscinny", after: null },
      { query: "goscinny", after: asterixLeGaulois.id },
    ]);
  });

  it.skip("S6 The search runs when the reader pauses", async () => {
    // Given
    const search = librisAnswers({
      goscinny: [found(asterixLeGaulois, laSerpeDOr)],
    });
    const screen = await theCatalogueOpen(search);

    // When
    vi.useFakeTimers({ toFake: ["setTimeout", "clearTimeout"] });
    await typeAtSpeed(screen, "goscinny");

    // Then
    expect(search.asked).toEqual([]);

    // When
    vi.advanceTimersByTime(300);
    vi.useRealTimers();

    // Then
    await waitFor(() =>
      expect(rows(screen)).toEqual([
        reads(asterixLeGaulois),
        reads(laSerpeDOr),
      ]),
    );
    expect(search.asked).toEqual([{ query: "goscinny", after: null }]);
  });

  it.skip("S6 The search runs when the reader pauses, the search key", async () => {
    // Given
    const search = librisAnswers({
      goscinny: [found(asterixLeGaulois, laSerpeDOr)],
    });
    const screen = await theCatalogueOpen(search);

    // When
    vi.useFakeTimers({ toFake: ["setTimeout", "clearTimeout"] });
    await type(screen, "goscinny");
    await pressTheSearchKey(screen);
    vi.useRealTimers();

    // Then
    await waitFor(() =>
      expect(rows(screen)).toEqual([
        reads(asterixLeGaulois),
        reads(laSerpeDOr),
      ]),
    );
    expect(search.asked).toEqual([{ query: "goscinny", after: null }]);
  });

  it.skip("S6 The search runs when the reader pauses, one character", async () => {
    // Given
    const search = librisAnswers({
      goscinny: [found(asterixLeGaulois, laSerpeDOr)],
    });
    const screen = await theCatalogueOpen(search);
    await type(screen, "goscinny");
    await waitFor(() =>
      expect(rows(screen)).toEqual([
        reads(asterixLeGaulois),
        reads(laSerpeDOr),
      ]),
    );

    // When
    vi.useFakeTimers({ toFake: ["setTimeout", "clearTimeout"] });
    await type(screen, " g ");
    vi.advanceTimersByTime(300);
    vi.useRealTimers();

    // Then
    await waitFor(() => expect(rows(screen)).toEqual(theWholeCatalogue));
    expect(search.asked).toEqual([{ query: "goscinny", after: null }]);
  });

  it.skip("S6 The search runs when the reader pauses, the cross", async () => {
    // Given
    const search = librisAnswers({
      goscinny: [found(asterixLeGaulois, laSerpeDOr)],
    });
    const screen = await theCatalogueOpen(search);
    await type(screen, "goscinny");
    await waitFor(() =>
      expect(rows(screen)).toEqual([
        reads(asterixLeGaulois),
        reads(laSerpeDOr),
      ]),
    );

    // When
    vi.useFakeTimers({ toFake: ["setTimeout", "clearTimeout"] });
    await fireEvent.click(
      screen.getByRole("button", { name: "Effacer la recherche" }),
    );
    vi.useRealTimers();

    // Then
    await waitFor(() => expect(rows(screen)).toEqual(theWholeCatalogue));
    expect(box(screen).value).toBe("");
    expect(document.activeElement).toBe(box(screen));
  });

  it.skip("S7 The search survives a visit", async () => {
    // Given
    const search = librisAnswers({
      goscinny: [
        found(asterixLeGaulois, laSerpeDOr),
        found(asterixLeGaulois, laSerpeDOr),
      ],
    });
    const screen = await theCatalogueOpen(search);
    await type(screen, "goscinny");
    await waitFor(() =>
      expect(rows(screen)).toEqual([
        reads(asterixLeGaulois),
        reads(laSerpeDOr),
      ]),
    );
    await waitFor(() => expect(address()).toBe("/catalogue?q=goscinny"));

    // When
    await fireEvent.click(screen.getByRole("link", { name: /La serpe d'or/ }));
    await screen.findByRole("heading", { name: "La serpe d'or" });
    window.history.back();

    // Then
    await waitFor(() =>
      expect(rows(screen)).toEqual([
        reads(asterixLeGaulois),
        reads(laSerpeDOr),
      ]),
    );
    expect(box(screen).value).toBe("goscinny");
  });

  it.skip("S7 The search survives a visit, two searches", async () => {
    // Given
    const search = librisAnswers({
      goscinny: [found(asterixLeGaulois, laSerpeDOr)],
      oda: [found(romanceDawn)],
    });
    const screen = await theCatalogueOpen(search);
    const entries = window.history.length;

    // When
    await type(screen, "goscinny");
    await waitFor(() =>
      expect(rows(screen)).toEqual([
        reads(asterixLeGaulois),
        reads(laSerpeDOr),
      ]),
    );
    await type(screen, "oda");
    await waitFor(() => expect(rows(screen)).toEqual([reads(romanceDawn)]));

    // Then
    await waitFor(() => expect(address()).toBe("/catalogue?q=oda"));
    expect(window.history.length).toBe(entries);
  });

  it.skip("S7 The search survives a visit, Séries then Ouvrages", async () => {
    // Given
    const search = librisAnswers({
      goscinny: [found(asterixLeGaulois, laSerpeDOr)],
    });
    const screen = await theCatalogueOpen(search);
    await type(screen, "goscinny");
    await waitFor(() =>
      expect(rows(screen)).toEqual([
        reads(asterixLeGaulois),
        reads(laSerpeDOr),
      ]),
    );

    // When
    await fireEvent.click(
      viewSwitch(screen).getByRole("link", { name: "Séries" }),
    );
    await screen.findByText(
      "Les séries de vos bibliothèques apparaîtront ici.",
    );
    await fireEvent.click(
      viewSwitch(screen).getByRole("link", { name: "Ouvrages" }),
    );

    // Then
    await waitFor(() => expect(rows(screen)).toEqual(theWholeCatalogue));
    expect(box(screen).value).toBe("");
  });

  it.skip("S8 Only the latest search counts", async () => {
    // Given
    const goscinny = later();
    const oda = later();
    const search = librisAnswers({
      goscinny: [goscinny.page],
      oda: [oda.page],
    });
    const screen = await theCatalogueOpen(search);
    await type(screen, "goscinny");
    await waitFor(() => expect(search.asked).toHaveLength(1));
    await type(screen, "oda");
    await waitFor(() => expect(search.asked).toHaveLength(2));

    // When
    oda.arrives(found(romanceDawn));
    await settled();
    goscinny.arrives(found(asterixLeGaulois, laSerpeDOr));
    await settled();

    // Then
    expect(rows(screen)).toEqual([reads(romanceDawn)]);
  });

  it.skip("S9 Libris unavailable", async () => {
    // Given
    const search = librisAnswers({
      goscinny: [new TypeError("Failed to fetch")],
    });
    const screen = await theCatalogueOpen(search);

    // When
    await type(screen, "goscinny");

    // Then
    await screen.findByText(
      "Erreur lors de la recherche, veuillez réessayer plus tard.",
    );
    expect(rows(screen)).toEqual([]);
    expect(box(screen).value).toBe("goscinny");
  });

  it.skip("S9 Libris unavailable, on the next page", async () => {
    // Given
    const search = librisAnswers({
      goscinny: [
        { books: [asterixLeGaulois], next: asterixLeGaulois.id, near: false },
        new TypeError("Failed to fetch"),
      ],
    });
    const screen = await theCatalogueOpen(search);
    await type(screen, "goscinny");
    await waitFor(() =>
      expect(rows(screen)).toEqual([reads(asterixLeGaulois)]),
    );

    // When
    theLastRowComesIntoView();

    // Then
    await screen.findByText(
      "Erreur lors de la recherche, veuillez réessayer plus tard.",
    );
    expect(rows(screen)).toEqual([reads(asterixLeGaulois)]);
  });

  it("S10 No search box over an empty catalogue", async () => {
    // Given
    const catalogue = new FakeCatalogueApi([{ books: [], next: null }]);

    // When
    const screen = open({ catalogue });

    // Then
    await screen.findByText(
      "Les ouvrages de vos bibliothèques apparaîtront ici.",
    );
    expect(screen.queryByRole("searchbox")).toBeNull();
  });

  const asterixLeGaulois: Book = book({
    id: "6f1d2c3b-4a59-4e6f-8b70-1c2d3e4f5a61",
    isbn13: "9782012101333",
    kind: "BD",
    title: "Astérix le Gaulois",
    series: { name: "Astérix", volumeNumber: 1 },
  });

  const laSerpeDOr: Book = book({
    id: "8b3f4e5d-6c7b-4a81-9d92-3e4f5a6b7c83",
    isbn13: "9782012101340",
    kind: "BD",
    title: "La serpe d'or",
    series: { name: "Astérix", volumeNumber: 2 },
  });

  const romanceDawn: Book = book({
    id: "5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50",
    isbn13: "9782723488525",
    kind: "MANGA",
    title: "Romance dawn",
    authors: [{ name: "Eiichirō Oda", role: "WRITER" }],
    series: { name: "One piece", volumeNumber: 1 },
  });

  const theWholeCatalogue = [
    reads(asterixLeGaulois),
    reads(laSerpeDOr),
    reads(romanceDawn),
  ];

  function book(fields: Partial<Book> & Pick<Book, "id" | "title">): Book {
    return {
      isbn13: null,
      kind: "BOOK",
      subtitle: null,
      authors: [
        { name: "René Goscinny", role: "WRITER" },
        { name: "Albert Uderzo", role: "ARTIST" },
      ],
      series: null,
      collection: null,
      publisher: null,
      publicationYear: null,
      language: null,
      pageCount: null,
      summary: null,
      coverUrl: null,
      copies: [on(bibliothequeDeLea)],
      ...fields,
    };
  }

  function on(bookshelf: Bookshelf): Copy {
    return {
      id: crypto.randomUUID(),
      bookshelf: { ...bookshelf, role: "OWNER" },
    };
  }

  function librisAnswers(
    answers: Record<string, (SearchPage | Error | Promise<SearchPage>)[]>,
  ): FakeSearchApi {
    return new FakeSearchApi(answers);
  }

  function found(...books: Book[]): SearchPage {
    return { books, next: null, near: false };
  }

  function near(...books: Book[]): SearchPage {
    return { books, next: null, near: true };
  }

  function nothing(): SearchPage {
    return { books: [], next: null, near: false };
  }

  function later() {
    let arrives: (page: SearchPage) => void = () => {};
    const page = new Promise<SearchPage>((resolve) => {
      arrives = resolve;
    });
    return { page, arrives };
  }

  function settled() {
    return new Promise((resolve) => setTimeout(resolve));
  }

  const observed: {
    callback: IntersectionObserverCallback;
    target: Element;
  }[] = [];

  function theLastRowComesIntoView() {
    const last = observed.at(-1);
    if (last === undefined) throw Error("No row is observed");
    last.callback(
      [
        {
          isIntersecting: true,
          target: last.target,
        } as IntersectionObserverEntry,
      ],
      {} as IntersectionObserver,
    );
  }

  async function theCatalogueOpen(search: SearchApi) {
    const screen = open({ search });
    await waitFor(() => expect(rows(screen)).toEqual(theWholeCatalogue));
    return screen;
  }

  function open(world: { catalogue?: CatalogueApi; search?: SearchApi }) {
    observed.length = 0;
    vi.stubGlobal(
      "IntersectionObserver",
      class {
        constructor(private readonly callback: IntersectionObserverCallback) {}
        observe(target: Element) {
          observed.push({ callback: this.callback, target });
        }
        unobserve() {}
        disconnect() {}
      },
    );
    window.history.replaceState(null, "", "/catalogue");
    app = createLibrisApp(
      {
        meApi: new FakeMeApi(lea),
        isbnApi: new FakeIsbnApi({
          outcome: "problem",
          type: "/problems/not-found",
        }),
        bookshelfApi: new FakeBookshelfApi(
          new Error("no add in this scenario"),
        ),
        barcodeScanner: new FakeBarcodeScanner(false),
        appUpdate: new FakeAppUpdate(),
        catalogueApi:
          world.catalogue ??
          new FakeCatalogueApi(
            [
              {
                books: [asterixLeGaulois, laSerpeDOr, romanceDawn],
                next: null,
              },
            ],
            { outcome: "found", book: laSerpeDOr },
          ),
        bookshelvesApi: new FakeBookshelvesApi({ memberships: [] }),
        readersApi: new FakeReadersApi([]),
        seriesApi: new FakeSeriesApi([]),
        copiesApi: new FakeCopiesApi(new Error("no removal in this scenario")),
        beeper: new FakeBeeper(),
        beepSetting: new FakeBeepSetting(),
        authorsApi: new FakeAuthorsApi([]),
        editionsApi: new FakeEditionsApi(new Error("no save in this scenario")),
      },
      "sha-abc1234",
    );
    app.provide(searchApiKey, world.search ?? new FakeSearchApi());
    app.mount(host);
    return within(host);
  }

  function box(screen: Screen) {
    return screen.getByRole<HTMLInputElement>("searchbox", {
      name: "Rechercher dans le catalogue",
    });
  }

  async function type(screen: Screen, text: string) {
    await fireEvent.input(box(screen), { target: { value: text } });
  }

  async function typeAtSpeed(screen: Screen, text: string) {
    for (let end = 1; end <= text.length; end++) {
      if (end > 1) vi.advanceTimersByTime(100);
      await type(screen, text.slice(0, end));
    }
  }

  async function pressTheSearchKey(screen: Screen) {
    const input = box(screen);
    const submits = await fireEvent.keyDown(input, { key: "Enter" });
    if (submits && input.form !== null) await fireEvent.submit(input.form);
    await fireEvent.keyUp(input, { key: "Enter" });
  }

  function address() {
    return window.location.pathname + window.location.search;
  }

  function viewSwitch(screen: Screen) {
    return within(screen.getByRole("navigation", { name: "Vue du catalogue" }));
  }

  function rows(screen: Screen) {
    return screen
      .queryAllByRole("listitem")
      .map((row) => row.textContent?.replace(/\s+/g, " ").trim() ?? "");
  }

  function reads(book: Book) {
    const overline =
      book.series === null
        ? []
        : [
            book.series.volumeNumber === null
              ? book.series.name
              : `${book.series.name} · ${book.kind === "BD" ? "album" : "tome"} ${book.series.volumeNumber}`,
          ];
    const authors = book.authors.map((author) => author.name).join(", ");
    const bookshelves = [...new Set(book.copies.map((copy) => copy.bookshelf))]
      .map((bookshelf) => {
        const held = book.copies.filter((copy) => copy.bookshelf === bookshelf);
        return held.length > 1
          ? `${bookshelf.name} · ${held.length} exemplaires`
          : bookshelf.name;
      })
      .join(", ");
    return [...overline, book.title, authors, bookshelves].join(" ");
  }
});
