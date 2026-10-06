import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { App } from "vue";
import type { BookshelfApi } from "../application/BookshelfApi";
import type { CatalogueApi } from "../application/CatalogueApi";
import type { IsbnApi } from "../application/IsbnApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Book } from "../domain/Book";
import type { Copy } from "../domain/Copy";
import type { CoverCandidate } from "../domain/Cover";
import type { SourceEdition } from "../domain/SourceEdition";
import { asterixLeGaulois, bibliothequeDeLea } from "../fixture/Books";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import { FakeBarcodeScanner } from "../fixture/FakeBarcodeScanner";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeCatalogueApi } from "../fixture/FakeCatalogueApi";
import { FakeIsbnApi } from "../fixture/FakeIsbnApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { lea } from "../fixture/Readers";
import { onePiece1 } from "../fixture/SourceEditions";

type Screen = BoundFunctions<typeof queries>;

const inventaire: CoverCandidate = {
  source: "inventaire.io",
  url: "https://inventaire.io/img/entities/480x600/34d6e7d99cec5b0922b9eccfeb03748ab2b4db99",
};

const openLibrary: CoverCandidate = {
  source: "Open Library",
  url: "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg?default=false",
};

const bnf: CoverCandidate = {
  source: "BnF",
  url: "https://catalogue.bnf.fr/couverture?&appName=NE&idArk=ark:/12148/cb43636708p&couverture=1",
};

const libris: CoverCandidate = {
  source: "Libris",
  url: "/api/v1/covers/9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
};

describe("Covers", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
    vi.unstubAllGlobals();
  });

  it.skip("S1 The lookup offers the sources' covers", async () => {
    // Given
    const screen = open("/isbn", {
      isbn: theSourcesOffer(onePiece1, [inventaire, openLibrary, bnf]),
    });

    // When
    await ask(screen, "9782723488525");

    // Then
    await screen.findByText("Romance dawn");
    expect(coverImage(screen).getAttribute("src")).toBe(inventaire.url);
  });

  it.skip("S2 The card shows the first cover that loads", async () => {
    // Given
    const screen = open("/isbn", {
      isbn: theSourcesOffer(onePiece1, [inventaire, openLibrary, bnf]),
    });
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");

    // When
    await loads(coverImage(screen));
    await loads(coverImage(screen));
    await loads(coverImage(screen));

    // Then
    expect(coverImage(screen).getAttribute("src")).toBe(inventaire.url);
    expect(sourceName(screen)).toBe("inventaire.io");
    expect(dots(screen).map((dot) => dot.getAttribute("aria-label"))).toEqual(
      ["Couverture inventaire.io", "Couverture Open Library", "Couverture BnF"],
    );
    expect(dots(screen).map(isFilled)).toEqual([true, false, false]);
  });

  it.skip("S2 The card shows the first cover that loads, the reader switches", async () => {
    // Given
    const screen = open("/isbn", {
      isbn: theSourcesOffer(onePiece1, [inventaire, openLibrary, bnf]),
    });
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");
    await loads(coverImage(screen));
    await loads(coverImage(screen));
    await loads(coverImage(screen));

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "Couverture Open Library" }),
    );

    // Then
    expect(coverImage(screen).getAttribute("src")).toBe(openLibrary.url);
    expect(sourceName(screen)).toBe("Open Library");
    expect(dots(screen).map(isFilled)).toEqual([false, true, false]);
  });

  it.skip("S2 The card shows the first cover that loads, a candidate does not load", async () => {
    // Given
    const screen = open("/isbn", {
      isbn: theSourcesOffer(onePiece1, [inventaire, openLibrary, bnf]),
    });
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");

    // When
    await fails(coverImage(screen));
    await loads(coverImage(screen));
    await loads(coverImage(screen));

    // Then
    expect(coverImage(screen).getAttribute("src")).toBe(openLibrary.url);
    expect(sourceName(screen)).toBe("Open Library");
    expect(dots(screen).map((dot) => dot.getAttribute("aria-label"))).toEqual(
      ["Couverture Open Library", "Couverture BnF"],
    );
  });

  it.skip("S2 The card shows the first cover that loads, none loads", async () => {
    // Given
    const screen = open("/isbn", {
      isbn: theSourcesOffer(onePiece1, [inventaire, openLibrary, bnf]),
    });
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");

    // When
    await fails(coverImage(screen));
    await fails(coverImage(screen));
    await fails(coverImage(screen));

    // Then
    expect(screen.queryByRole("img", { name: "Couverture de Romance dawn" }))
      .toBeNull();
    expect(dots(screen)).toEqual([]);
    expect(sourceName(screen)).toBeNull();
  });

  it.skip("S3 The add carries the cover's source", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = open("/isbn", {
      isbn: theSourcesOffer(onePiece1, [inventaire, openLibrary, bnf]),
      add,
    });
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");
    await fails(coverImage(screen));
    await loads(coverImage(screen));

    // When
    await fireEvent.click(addButton(screen));

    // Then
    await screen.findByText("Dans Bibliothèque de Léa");
    expect(add.asked.map((call) => call.coverSource)).toEqual(["Open Library"]);
    expect(coverImage(screen).getAttribute("src")).toBe(openLibrary.url);
    expect(sourceName(screen)).toBe("Open Library");
  });

  it.skip("S3 The add carries the cover's source, the stand-in", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = open("/isbn", {
      isbn: theSourcesOffer(onePiece1, [inventaire]),
      add,
    });
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");
    await fails(coverImage(screen));

    // When
    await fireEvent.click(addButton(screen));

    // Then
    await screen.findByText("Dans Bibliothèque de Léa");
    expect(add.asked.map((call) => call.coverSource)).toEqual([null]);
  });

  it.skip("S10 A held edition offers its own cover", async () => {
    // Given
    const screen = open("/isbn", {
      isbn: theHouseHolds(onePiece1, [on(lea.defaultBookshelf)], [libris]),
    });

    // When
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");
    await loads(coverImage(screen));

    // Then
    expect(coverImage(screen).getAttribute("src")).toBe(libris.url);
    expect(dots(screen)).toEqual([]);
    expect(sourceName(screen)).toBeNull();
    expect(screen.queryByText("Libris")).toBeNull();
  });

  it.skip("S10 A held edition offers its own cover, not yet stored", async () => {
    // Given
    const screen = open("/isbn", {
      isbn: theHouseHolds(onePiece1, [on(lea.defaultBookshelf)], []),
    });

    // When
    await ask(screen, "9782723488525");

    // Then
    await screen.findByText("Romance dawn");
    expect(screen.queryByRole("img", { name: "Couverture de Romance dawn" }))
      .toBeNull();
    expect(dots(screen)).toEqual([]);
  });

  it.skip("S11 The catalogue shows the covers", async () => {
    // Given
    const withCover: Book = { ...asterixLeGaulois, coverUrl: libris.url };
    const withoutCover: Book = {
      ...asterixLeGaulois,
      id: "7a2e3d4c-5b6a-4f70-9c81-2d3e4f5a6b72",
      title: "La serpe d'or",
      series: { name: "Astérix", volumeNumber: 2 },
      coverUrl: null,
    };

    // When
    const screen = open("/catalogue", {
      isbn: theSourcesOffer(onePiece1, []),
      catalogue: librisLists(withCover, withoutCover),
    });

    // Then
    await screen.findByText("La serpe d'or");
    const [first, second] = screen.getAllByRole("listitem");
    expect(
      within(first)
        .getByRole("img", { name: "Couverture de Astérix le Gaulois" })
        .getAttribute("src"),
    ).toBe(libris.url);
    expect(within(second).queryByRole("img")).toBeNull();
  });

  function theSourcesOffer(
    edition: SourceEdition,
    covers: CoverCandidate[],
  ): IsbnApi {
    return new FakeIsbnApi({
      outcome: "found",
      id: null,
      edition,
      copies: [],
      covers,
    });
  }

  function theHouseHolds(
    edition: SourceEdition,
    copies: Copy[],
    covers: CoverCandidate[],
  ): IsbnApi {
    return new FakeIsbnApi({
      outcome: "found",
      id: "3c4d5e6f-7a8b-4c9d-8e0f-1a2b3c4d5e6f",
      edition,
      copies,
      covers,
    });
  }

  function librisAdds(copy: Copy): FakeBookshelfApi {
    return new FakeBookshelfApi({ outcome: "added", copy });
  }

  function librisLists(...books: Book[]): CatalogueApi {
    return new FakeCatalogueApi([{ books, next: null }]);
  }

  function on(bookshelf: typeof bibliothequeDeLea): Copy {
    return { id: crypto.randomUUID(), bookshelf };
  }

  function open(
    path: string,
    world: { isbn: IsbnApi; add?: BookshelfApi; catalogue?: CatalogueApi },
  ) {
    vi.stubGlobal(
      "IntersectionObserver",
      class {
        observe() {}
        unobserve() {}
        disconnect() {}
      },
    );
    window.history.replaceState(null, "", path);
    app = createLibrisApp(
      {
        meApi: new FakeMeApi(lea),
        isbnApi: world.isbn,
        bookshelfApi:
          world.add ??
          new FakeBookshelfApi(new Error("no add in this scenario")),
        barcodeScanner: new FakeBarcodeScanner(false),
        appUpdate: new FakeAppUpdate(),
        catalogueApi: world.catalogue ?? new FakeCatalogueApi([]),
      },
      "sha-abc1234",
    );
    app.mount(host);
    return within(host);
  }

  async function ask(screen: Screen, text: string) {
    await fireEvent.input(
      screen.getByRole<HTMLInputElement>("textbox", { name: "ISBN" }),
      { target: { value: text } },
    );
    await fireEvent.click(screen.getByRole("button", { name: "Chercher" }));
  }

  function addButton(screen: Screen) {
    return screen.getByRole("button", { name: "Ajouter à ma bibliothèque" });
  }

  function coverImage(screen: Screen) {
    return screen.getByRole("img", { name: "Couverture de Romance dawn" });
  }

  async function loads(image: Element) {
    await fireEvent.load(image);
  }

  async function fails(image: Element) {
    await fireEvent.error(image);
  }

  function dots(screen: Screen) {
    return screen.queryAllByRole("button", { name: /^Couverture / });
  }

  function isFilled(dot: Element) {
    return dot.getAttribute("aria-pressed") === "true";
  }

  function sourceName(screen: Screen) {
    return (
      screen.queryByText(/^(inventaire\.io|Open Library|BnF)$/)?.textContent ??
      null
    );
  }
});
