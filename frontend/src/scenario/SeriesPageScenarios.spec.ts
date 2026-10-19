import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { App } from "vue";
import type { SeriesApi } from "../application/SeriesApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Book } from "../domain/Book";
import type { SeriesWithBooks } from "../domain/Series";
import type { Kind, SourceAuthor } from "../domain/SourceEdition";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import {
  asterixEtSesAmis,
  asterixLeGaulois,
  bibliothequeDeLea,
  salon,
} from "../fixture/Books";
import { FakeBarcodeScanner } from "../fixture/FakeBarcodeScanner";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeBookshelvesApi } from "../fixture/FakeBookshelvesApi";
import { FakeCatalogueApi } from "../fixture/FakeCatalogueApi";
import { FakeIsbnApi } from "../fixture/FakeIsbnApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { FakeReadersApi } from "../fixture/FakeReadersApi";
import { FakeSeriesApi } from "../fixture/FakeSeriesApi";
import { lea } from "../fixture/Readers";

type Screen = BoundFunctions<typeof queries>;

const laSerpeDOr: Book = {
  id: "2d4e6f80-91a2-4b3c-8d4e-5f60718293a4",
  isbn13: null,
  kind: "BD",
  title: "La Serpe d'or",
  subtitle: null,
  authors: [
    { name: "René Goscinny", role: "WRITER" },
    { name: "Albert Uderzo", role: "ARTIST" },
  ],
  series: { name: "Astérix", volumeNumber: 2 },
  collection: null,
  publisher: null,
  publicationYear: null,
  language: null,
  pageCount: null,
  summary: null,
  coverUrl: null,
  copies: [{ id: "3e5f7091-a2b3-4c4d-9e5f-60718293a4b5", bookshelf: salon }],
};

const asterix: SeriesWithBooks = {
  id: "4f60718a-b2c3-4d4e-8f60-718293a4b5c6",
  name: "Astérix",
  volumeCount: 3,
  coverUrl: null,
  kind: "BD",
  books: [asterixEtSesAmis, laSerpeDOr, asterixLeGaulois],
};

const withCover: SeriesWithBooks = {
  ...asterix,
  coverUrl:
    "/api/v1/covers/63dacaf17234db7995b80673af35f7cebb45ba7c0b514c11ffd8b143fc05ec27",
};

describe("Series page", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
    vi.unstubAllGlobals();
  });

  it("S1 The reader opens a series", async () => {
    // Given
    const screen = open("/catalogue/series", librisAnswers(asterix));
    await screen.findByText("Astérix");

    // When
    await fireEvent.click(screen.getByText("Astérix"));

    // Then
    await screen.findByRole("heading", { name: "Astérix" });
    expect(window.location.pathname).toBe(`/catalogue/series/${asterix.id}`);
    expect(screen.getByText("3 albums dans vos bibliothèques.")).toBeDefined();
    expect(rowsOfTheListHolding(screen, "La Serpe d'or")).toEqual([
      "Album 1 Astérix le Gaulois René Goscinny, Albert Uderzo Bibliothèque de Léa",
      "Album 2 La Serpe d'or René Goscinny, Albert Uderzo Salon",
      "Astérix et ses amis René Goscinny, Albert Uderzo Salon",
    ]);
  });

  it.each([
    {
      case: "tomes 1 to 25, 36, 37 and 50",
      owned: owns("One piece", "MANGA", [...from(1, 25), 36, 37, 50]),
      fields: [
        "Tomes possédés 1 à 25, 36, 37, 50",
        "Tomes manquants 26 à 35, 38 à 49",
      ],
      rows: [
        ...from(1, 25).map(
          (n) => `Tome ${n} One piece ${n} Bibliothèque de Léa`,
        ),
        "Tomes 26 à 35 Manquants",
        "Tome 36 One piece 36 Bibliothèque de Léa",
        "Tome 37 One piece 37 Bibliothèque de Léa",
        "Tomes 38 à 49 Manquants",
        "Tome 50 One piece 50 Bibliothèque de Léa",
      ],
    },
    {
      case: "a BD series in albums",
      owned: owns("Blake et Mortimer", "BD", [1, 2, 4]),
      fields: ["Albums possédés 1, 2, 4", "Albums manquants 3"],
      rows: [
        "Album 1 Blake et Mortimer 1 Bibliothèque de Léa",
        "Album 2 Blake et Mortimer 2 Bibliothèque de Léa",
        "Album 3 Manquant",
        "Album 4 Blake et Mortimer 4 Bibliothèque de Léa",
      ],
    },
    {
      case: "a series without a gap",
      owned: owns("Dragon quest", "MANGA", [1, 2, 3]),
      fields: ["Tomes possédés 1 à 3"],
      rows: [
        "Tome 1 Dragon quest 1 Bibliothèque de Léa",
        "Tome 2 Dragon quest 2 Bibliothèque de Léa",
        "Tome 3 Dragon quest 3 Bibliothèque de Léa",
      ],
    },
  ])(
    "S2 The owned tomes and the gaps, $case",
    async ({ owned, fields, rows }) => {
      // Given
      const libris = librisAnswers(owned);

      // When
      const screen = open(`/catalogue/series/${owned.id}`, libris);

      // Then
      await screen.findByRole("heading", { name: owned.name });
      expect(ownedAndMissing()).toEqual(fields);
      expect(rowsOfTheListHolding(screen, `${owned.name} 1`)).toEqual(rows);
    },
  );

  it.skip("S3 The authors and the publishers", async () => {
    // Given
    const editions: SeriesWithBooks = {
      id: "6b7c8d9e-0f1a-4b2c-9d3e-4f5a6b7c8d9e",
      name: "Astérix",
      volumeCount: 4,
      coverUrl: null,
      kind: "BD",
      books: [
        album(1, "Astérix le Gaulois", "Hachette", [
          { name: "René Goscinny", role: "WRITER" },
          { name: "Albert Uderzo", role: "ARTIST" },
        ]),
        album(1, "Asterix the Gaul", "Orion", [
          { name: "René Goscinny", role: "WRITER" },
          { name: "Albert Uderzo", role: "ARTIST" },
          { name: "Anthea Bell", role: "TRANSLATOR" },
        ]),
        album(2, "La Serpe d'or", "Hachette", [
          { name: "René Goscinny", role: "WRITER" },
          { name: "Albert Uderzo", role: "ARTIST" },
        ]),
        album(37, "Astérix et la Transitalique", "Éditions Albert René", [
          { name: "Jean-Yves Ferri", role: "WRITER" },
          { name: "Didier Conrad", role: "ARTIST" },
        ]),
      ],
    };
    const libris = librisAnswers(editions);

    // When
    const screen = open(`/catalogue/series/${editions.id}`, libris);

    // Then
    await screen.findByRole("heading", { name: "Astérix" });
    expect(words(host)).toContain(
      "Auteurs Jean-Yves Ferri, René Goscinny, Albert Uderzo, Didier Conrad " +
        "Éditeurs Éditions Albert René, Hachette, Orion",
    );
  });

  it.skip("S4 The band of a series", async () => {
    // Given
    const libris = librisAnswers(withCover);

    // When
    const screen = open(`/catalogue/series/${withCover.id}`, libris);

    // Then
    const name = await screen.findByRole("heading", { name: "Astérix" });
    const band = bandOf(screen);
    expect(band?.getAttribute("src")).toBe(withCover.coverUrl);
    expect(band?.compareDocumentPosition(name)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING,
    );
  });

  it.skip("S4 The band of a series, no cover", async () => {
    // Given
    const libris = librisAnswers(asterix);

    // When
    const screen = open(`/catalogue/series/${asterix.id}`, libris);

    // Then
    await screen.findByRole("heading", { name: "Astérix" });
    expect(bandOf(screen)).toBeNull();
  });

  it.skip("S4 The band of a series, a cover that does not load", async () => {
    // Given
    const screen = open(
      `/catalogue/series/${withCover.id}`,
      librisAnswers(withCover),
    );
    await screen.findByRole("heading", { name: "Astérix" });

    // When
    await fireEvent.error(
      screen.getByRole("img", { name: "Couverture de Astérix" }),
    );

    // Then
    expect(bandOf(screen)).toBeNull();
    expect(screen.getByRole("heading", { name: "Astérix" })).toBeDefined();
  });

  it.each([
    {
      case: "Libris answers no such series",
      address: "/catalogue/series/7c8d9e0f-1a2b-4c3d-8e4f-5a6b7c8d9e0f",
      problem: "/problems/not-found",
    },
    {
      case: "an id that is not one",
      address: "/catalogue/series/tintin",
      problem: "/problems/validation",
    },
  ])("S5 No such series, $case", async ({ address, problem }) => {
    // Given
    const libris = librisRefuses(problem);

    // When
    const screen = open(address, libris);

    // Then
    await screen.findByText("Série introuvable.");
    expect(screen.queryByRole("heading")).toBeNull();
    expect(bandOf(screen)).toBeNull();
    expect(screen.queryAllByRole("listitem")).toEqual([]);
  });

  it.skip("S6 Libris unavailable", async () => {
    // Given
    const libris = librisDoesNotAnswer();

    // When
    const screen = open(`/catalogue/series/${asterix.id}`, libris);

    // Then
    await screen.findByText(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
    expect(screen.queryByRole("heading")).toBeNull();
    expect(bandOf(screen)).toBeNull();
    expect(screen.queryAllByRole("listitem")).toEqual([]);
  });

  function album(
    volumeNumber: number,
    title: string,
    publisher: string,
    authors: SourceAuthor[],
  ): Book {
    return {
      id: crypto.randomUUID(),
      isbn13: null,
      kind: "BD",
      title,
      subtitle: null,
      authors,
      series: { name: "Astérix", volumeNumber },
      collection: null,
      publisher,
      publicationYear: null,
      language: null,
      pageCount: null,
      summary: null,
      coverUrl: null,
      copies: [
        {
          id: crypto.randomUUID(),
          bookshelf: bibliothequeDeLea,
        },
      ],
    };
  }

  function owns(name: string, kind: Kind, tomes: number[]): SeriesWithBooks {
    return {
      id: "5a6b7c8d-9e0f-4a1b-8c2d-3e4f5a6b7c8d",
      name,
      volumeCount: tomes.length,
      coverUrl: null,
      kind,
      books: tomes.map((tome) => ({
        id: crypto.randomUUID(),
        isbn13: null,
        kind,
        title: `${name} ${tome}`,
        subtitle: null,
        authors: [],
        series: { name, volumeNumber: tome },
        collection: null,
        publisher: null,
        publicationYear: null,
        language: null,
        pageCount: null,
        summary: null,
        coverUrl: null,
        copies: [
          {
            id: crypto.randomUUID(),
            bookshelf: bibliothequeDeLea,
          },
        ],
      })),
    };
  }

  function from(first: number, last: number) {
    return Array.from({ length: last - first + 1 }, (_, i) => first + i);
  }

  function librisAnswers(series: SeriesWithBooks): SeriesApi {
    return new FakeSeriesApi([series], { outcome: "found", series });
  }

  function librisRefuses(problem: string): SeriesApi {
    return new FakeSeriesApi([], { outcome: "problem", type: problem });
  }

  function librisDoesNotAnswer(): SeriesApi {
    return new FakeSeriesApi([], new TypeError("Failed to fetch"));
  }

  function open(path: string, series: SeriesApi) {
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
        isbnApi: new FakeIsbnApi({
          outcome: "problem",
          type: "/problems/not-found",
        }),
        bookshelfApi: new FakeBookshelfApi(
          new Error("no add in this scenario"),
        ),
        barcodeScanner: new FakeBarcodeScanner(false),
        appUpdate: new FakeAppUpdate(),
        catalogueApi: new FakeCatalogueApi([]),
        bookshelvesApi: new FakeBookshelvesApi({ memberships: [] }),
        readersApi: new FakeReadersApi([]),
        seriesApi: series,
      },
      "sha-abc1234",
    );
    app.mount(host);
    return within(host);
  }

  function bandOf(screen: Screen) {
    return screen.queryByRole("img", { name: "Couverture de Astérix" });
  }

  function ownedAndMissing() {
    return (
      words(host).match(
        /(Tomes|Albums) (possédés|manquants) \d([\d, à]*\d)?/g,
      ) ?? []
    );
  }

  function words(element: HTMLElement) {
    const texts = document.createTreeWalker(element, NodeFilter.SHOW_TEXT);
    const words: string[] = [];
    while (texts.nextNode()) {
      const text = texts.currentNode.textContent?.trim();
      if (text) words.push(text);
    }
    return words.join(" ");
  }

  function rowsOfTheListHolding(screen: Screen, text: string) {
    const list = screen
      .getAllByRole("list")
      .find((each) => within(each).queryByText(text) !== null);
    return list === undefined
      ? []
      : within(list).queryAllByRole("listitem").map(words);
  }
});
