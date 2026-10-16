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
import type { Series } from "../domain/Series";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import { asterixLeGaulois } from "../fixture/Books";
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

const onePiece: Series = {
  name: "One piece",
  volumeCount: 3,
  coverUrl:
    "/api/v1/covers/51aa137dfc73ec01925c94b5951649898ac8e50e911a392afb7af90baae87535",
  kind: "MANGA",
};

const asterix: Series = {
  name: "Astérix",
  volumeCount: 6,
  coverUrl:
    "/api/v1/covers/63dacaf17234db7995b80673af35f7cebb45ba7c0b514c11ffd8b143fc05ec27",
  kind: "BD",
};

const lesCarnetsDeLApothicaire: Series = {
  name: "Les Carnets de l'apothicaire",
  volumeCount: 1,
  coverUrl:
    "/api/v1/covers/bca2696091a9d263f2fc7dd08a8819fb719f07c1db310800be1aa5f1d9bcf293",
  kind: "MANGA",
};

const emileEtMargot: Series = {
  name: "Émile et Margot",
  volumeCount: 2,
  coverUrl:
    "/api/v1/covers/1195b7fd675382e4f9e5a8d43073b64554deaae6892fb890c66997ae4e8bcfd2",
  kind: "BD",
};

const lastman: Series = {
  name: "Lastman",
  volumeCount: 3,
  coverUrl:
    "/api/v1/covers/9a3c1f0e7b5d4e2a8c6b0d9f1e3a5c7b9d0f2e4a6c8b0d1f3e5a7c9b1d2f4e6a",
  kind: null,
};

const dragonQuest: Series = {
  name: "Dragon quest",
  volumeCount: 2,
  coverUrl: null,
  kind: "MANGA",
};

describe("Series list", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
    vi.unstubAllGlobals();
  });

  it.skip("S1 The reader's series", async () => {
    // Given
    const series = librisAnswers([
      onePiece,
      asterix,
      lesCarnetsDeLApothicaire,
      emileEtMargot,
    ]);

    // When
    const screen = open("/catalogue/series", series);

    // Then
    await screen.findByText("One piece");
    expect(rows(screen)).toEqual([
      "Astérix 6 albums",
      "Émile et Margot 2 albums",
      "Les Carnets de l'apothicaire 1 tome",
      "One piece 3 tomes",
    ]);
    expect(coverOf(screen, "One piece").getAttribute("src")).toBe(
      onePiece.coverUrl,
    );
  });

  it.skip.each([
    { case: "a BD series", series: asterix, then: "Astérix 6 albums" },
    {
      case: "a manga series",
      series: lesCarnetsDeLApothicaire,
      then: "Les Carnets de l'apothicaire 1 tome",
    },
    {
      case: "a series mixing a BD and a manga",
      series: lastman,
      then: "Lastman 3 tomes",
    },
  ])("S2 The count of a series, $case", async ({ series, then }) => {
    // Given
    const libris = librisAnswers([series]);

    // When
    const screen = open("/catalogue/series", libris);

    // Then
    await screen.findByText(series.name);
    expect(rows(screen)).toEqual([then]);
  });

  it.skip("S3 The cover of a series, the first tome without a cover", async () => {
    // Given
    const series = librisAnswers([dragonQuest]);

    // When
    const screen = open("/catalogue/series", series);

    // Then
    await screen.findByText("Dragon quest");
    expect(within(screen.getByRole("listitem")).queryByRole("img")).toBeNull();
  });

  it.skip("S4 The reader switches the view", async () => {
    // Given
    const screen = open("/", librisAnswers([asterix]), {
      catalogue: new FakeCatalogueApi([
        { books: [asterixLeGaulois], next: null },
        { books: [asterixLeGaulois], next: null },
      ]),
    });

    // When
    await fireEvent.click(screen.getByRole("link", { name: "Catalogue" }));
    const first = await shown(screen, "Astérix le Gaulois");
    await fireEvent.click(
      viewSwitch(screen).getByRole("link", { name: "Séries" }),
    );
    const second = await shown(screen, "Astérix");
    await fireEvent.click(
      viewSwitch(screen).getByRole("link", { name: "Ouvrages" }),
    );
    const third = await shown(screen, "Astérix le Gaulois");

    // Then
    expect([first, second, third]).toEqual([
      { address: "/catalogue", current: "Ouvrages" },
      { address: "/catalogue/series", current: "Séries" },
      { address: "/catalogue", current: "Ouvrages" },
    ]);
  });

  it("S5 No series", async () => {
    // Given
    const series = librisAnswers([]);

    // When
    const screen = open("/catalogue/series", series);

    // Then
    await screen.findByText(
      "Les séries de vos bibliothèques apparaîtront ici.",
    );
    expect(rows(screen)).toEqual([]);
  });

  it.skip("S6 Libris unavailable", async () => {
    // Given
    const series = librisDoesNotAnswer();

    // When
    const screen = open("/catalogue/series", series);

    // Then
    await screen.findByText(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
    expect(rows(screen)).toEqual([]);
  });

  function librisAnswers(series: Series[] | Error): SeriesApi {
    return new FakeSeriesApi(series);
  }

  function librisDoesNotAnswer(): SeriesApi {
    return librisAnswers(new TypeError("Failed to fetch"));
  }

  function open(
    path: string,
    series: SeriesApi,
    world: { catalogue?: CatalogueApi } = {},
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
        catalogueApi: world.catalogue ?? new FakeCatalogueApi([]),
        bookshelvesApi: new FakeBookshelvesApi({ memberships: [] }),
        readersApi: new FakeReadersApi([]),
        seriesApi: series,
      },
      "sha-abc1234",
    );
    app.mount(host);
    return within(host);
  }

  async function shown(screen: Screen, text: string) {
    await screen.findByText(text);
    return {
      address: window.location.pathname,
      current: viewSwitch(screen)
        .getByRole("link", { current: "page" })
        .textContent?.trim(),
    };
  }

  function viewSwitch(screen: Screen) {
    return within(screen.getByRole("navigation", { name: "Vue du catalogue" }));
  }

  function coverOf(screen: Screen, name: string) {
    return screen.getByRole("img", { name: `Couverture de ${name}` });
  }

  function rows(screen: Screen) {
    return screen.queryAllByRole("listitem").map(words);
  }

  function words(row: HTMLElement) {
    const texts = document.createTreeWalker(row, NodeFilter.SHOW_TEXT);
    const words: string[] = [];
    while (texts.nextNode()) {
      const text = texts.currentNode.textContent?.trim();
      if (text) words.push(text);
    }
    return words.join(" ");
  }
});
