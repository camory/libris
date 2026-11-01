import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, it } from "vitest";
import type { App } from "vue";
import type { CatalogueApi } from "../application/CatalogueApi";
import type { IsbnApi } from "../application/IsbnApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Book } from "../domain/Book";
import type { SourceEdition } from "../domain/SourceEdition";
import { bibliothequeDeLea } from "../fixture/Books";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
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

const lines = [
  "Le Mont Saint-Michel à l'origine de l'Apocalypse",
  "Depuis des siècles, le Mont Saint-Michel est la source de nombreuses légendes.",
  "Des druides celtes au crâne de l'évêque Aubert en passant par la forêt de Scissy, le lieutenant Clotilde Dumont pensait avoir tout entendu en prenant son poste à la gendarmerie du Mont… Mais ses idées sont vite chamboulées lorsque, suite à un éboulement, un mystérieux sanctuaire est mis à jour.",
];

const montTombe: SourceEdition = {
  isbn13: "9782344008980",
  kind: "BD",
  title: "Mont Tombe",
  subtitle: "la dernière légende du Mont-Saint-Michel",
  authors: [
    { name: "Izu", role: "WRITER" },
    { name: "Albert Sechao", role: "ARTIST" },
  ],
  series: null,
  collection: null,
  publisher: null,
  publicationYear: null,
  language: null,
  pageCount: null,
  summary: lines.join("\n"),
};

const montTombeHeld: Book = {
  ...montTombe,
  id: "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d",
  coverUrl: null,
  copies: [
    {
      id: "8c9d0e1f-2a3b-4c4d-9e5f-6a7b8c9d0e1f",
      bookshelf: bibliothequeDeLea,
    },
  ],
};

describe("Summary", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
  });

  it("S2 A summary in several lines, on the lookup", async () => {
    // Given
    const screen = open("/isbn", { isbn: theHouseHolds(montTombe) });

    // When
    await ask(screen, "9782344008980");

    // Then
    await screen.findByRole("heading", { name: "Mont Tombe" });
    expect(summaryParagraphs(screen)).toEqual(lines);
  });

  it("S2 A summary in several lines, on the ouvrage's page", async () => {
    // Given
    const catalogue = librisAnswers(montTombeHeld);

    // When
    const screen = open(`/catalogue/ouvrages/${montTombeHeld.id}`, {
      catalogue,
    });

    // Then
    await screen.findByRole("heading", { level: 1, name: "Mont Tombe" });
    expect(summaryParagraphs(screen)).toEqual(lines);
  });

  function theHouseHolds(edition: SourceEdition): IsbnApi {
    return new FakeIsbnApi({
      outcome: "found",
      id: montTombeHeld.id,
      edition,
      copies: [],
      covers: [],
    });
  }

  function librisAnswers(book: Book): CatalogueApi {
    return new FakeCatalogueApi([{ books: [book], next: null }], {
      outcome: "found",
      book,
    });
  }

  function open(
    path: string,
    world: { isbn?: IsbnApi; catalogue?: CatalogueApi },
  ) {
    window.history.replaceState(null, "", path);
    app = createLibrisApp(
      {
        meApi: new FakeMeApi(lea),
        isbnApi:
          world.isbn ??
          new FakeIsbnApi({ outcome: "problem", type: "/problems/not-found" }),
        bookshelfApi: new FakeBookshelfApi(
          new Error("no add in this scenario"),
        ),
        barcodeScanner: new FakeBarcodeScanner(false),
        appUpdate: new FakeAppUpdate(),
        catalogueApi: world.catalogue ?? new FakeCatalogueApi([]),
        bookshelvesApi: new FakeBookshelvesApi({ memberships: [] }),
        readersApi: new FakeReadersApi([]),
        seriesApi: new FakeSeriesApi([]),
      },
      "sha-abc1234",
    );
    app.mount(host);
    return within(host);
  }

  async function ask(screen: Screen, text: string) {
    const field = screen.getByRole<HTMLInputElement>("textbox", {
      name: "ISBN",
    });
    await fireEvent.input(field, { target: { value: text } });
    await fireEvent.click(screen.getByRole("button", { name: "Chercher" }));
  }

  function summaryParagraphs(screen: Screen) {
    return screen
      .getAllByRole("paragraph")
      .map((paragraph) => paragraph.textContent?.trim() ?? "")
      .filter((text) => lines.includes(text));
  }
});
