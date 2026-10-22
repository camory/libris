import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { App } from "vue";
import type { CatalogueApi } from "../application/CatalogueApi";
import type { SeriesApi } from "../application/SeriesApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Book } from "../domain/Book";
import type { SeriesWithBooks } from "../domain/Series";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import { asterixLeGaulois, bibliothequeDeLea, salon } from "../fixture/Books";
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

const held: Book = {
  ...asterixLeGaulois,
  copies: [
    {
      id: "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
      bookshelf: bibliothequeDeLea,
    },
    {
      id: "b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e",
      bookshelf: bibliothequeDeLea,
    },
    { id: "c3d4e5f6-a7b8-4c9d-8e1f-2a3b4c5d6e7f", bookshelf: salon },
  ],
};

const asterix: SeriesWithBooks = {
  id: "4f60718a-b2c3-4d4e-8f60-718293a4b5c6",
  name: "Astérix",
  volumeCount: 1,
  coverUrl: null,
  kind: "BD",
  books: [held],
};

describe("Edition page", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
    vi.unstubAllGlobals();
  });

  it.each([
    { case: "from a row of the catalogue", address: "/catalogue" },
    {
      case: "from a row of a series' page",
      address: `/catalogue/series/${asterix.id}`,
    },
  ])("S1 The reader opens an ouvrage, $case", async ({ address }) => {
    // Given
    const screen = open(address, {
      catalogue: librisAnswers(held),
      series: new FakeSeriesApi([asterix], {
        outcome: "found",
        series: asterix,
      }),
    });

    // When
    await fireEvent.click(
      await screen.findByRole("link", { name: /Astérix le Gaulois/ }),
    );

    // Then
    await screen.findByRole("heading", {
      level: 1,
      name: "Astérix le Gaulois",
    });
    expect(window.location.pathname).toBe(`/catalogue/ouvrages/${held.id}`);
    expect(rows(screen)).toEqual([
      "Dans Bibliothèque de Léa · 2 exemplaires",
      "Dans Salon",
    ]);
  });

  it.each([
    {
      case: "Libris answers no such ouvrage",
      address: "/catalogue/ouvrages/3e4f5a6b-7c8d-4e9f-a0b1-c2d3e4f5a6b7",
      problem: "/problems/not-found",
    },
    {
      case: "an id that is not one",
      address: "/catalogue/ouvrages/asterix",
      problem: "/problems/validation",
    },
  ])("S2 No such ouvrage, $case", async ({ address, problem }) => {
    // Given
    const catalogue = librisRefuses(problem);

    // When
    const screen = open(address, { catalogue });

    // Then
    await screen.findByText("Ouvrage introuvable.");
    expect(screen.queryByRole("heading")).toBeNull();
    expect(rows(screen)).toEqual([]);
  });

  it.skip("S3 Libris unavailable", async () => {
    // Given
    const catalogue = librisDoesNotAnswer();

    // When
    const screen = open(`/catalogue/ouvrages/${held.id}`, { catalogue });

    // Then
    await screen.findByText(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
    expect(screen.queryByRole("heading")).toBeNull();
    expect(rows(screen)).toEqual([]);
  });

  function librisAnswers(book: Book): CatalogueApi {
    return new FakeCatalogueApi([{ books: [book], next: null }], {
      outcome: "found",
      book,
    });
  }

  function librisRefuses(problem: string): CatalogueApi {
    return new FakeCatalogueApi([], { outcome: "problem", type: problem });
  }

  function librisDoesNotAnswer(): CatalogueApi {
    return new FakeCatalogueApi([], new TypeError("Failed to fetch"));
  }

  function open(
    path: string,
    world: { catalogue: CatalogueApi; series?: SeriesApi },
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
        isbnApi: new FakeIsbnApi({
          outcome: "problem",
          type: "/problems/not-found",
        }),
        bookshelfApi: new FakeBookshelfApi(
          new Error("no add in this scenario"),
        ),
        barcodeScanner: new FakeBarcodeScanner(false),
        appUpdate: new FakeAppUpdate(),
        catalogueApi: world.catalogue,
        bookshelvesApi: new FakeBookshelvesApi({ memberships: [] }),
        readersApi: new FakeReadersApi([]),
        seriesApi: world.series ?? new FakeSeriesApi([]),
      },
      "sha-abc1234",
    );
    app.mount(host);
    return within(host);
  }

  function rows(screen: Screen) {
    return screen
      .queryAllByText(/^Dans /)
      .map((row) => row.textContent?.trim() ?? "");
  }
});
