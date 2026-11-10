import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import type { App } from "vue";
import type { BookshelfApi } from "../application/BookshelfApi";
import type { CatalogueApi } from "../application/CatalogueApi";
import type { EditionsApi } from "../application/EditionsApi";
import type { IsbnApi } from "../application/IsbnApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Book } from "../domain/Book";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Copy } from "../domain/Copy";
import type { SourceEdition } from "../domain/SourceEdition";
import { asterixLeGaulois } from "../fixture/Books";
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
import { FakeSeriesApi } from "../fixture/FakeSeriesApi";
import { lea } from "../fixture/Readers";
import { onePiece2 } from "../fixture/SourceEditions";

type Screen = BoundFunctions<typeof queries>;
type Field = HTMLInputElement | HTMLTextAreaElement;

const asterix: Book = {
  ...asterixLeGaulois,
  isbn13: "9782012101333",
  series: null,
  publisher: "Hachette",
  publicationYear: 2004,
  language: "fr",
  pageCount: 48,
};

const trashName = { name: "Retirer un exemplaire" };

describe("Held edition edit", () => {
  const host = document.createElement("div");
  let app: App;

  beforeEach(() => {
    document.body.append(host);
    Object.defineProperty(Element.prototype, "scrollIntoView", {
      configurable: true,
      value: () => {},
    });
  });

  afterEach(() => {
    app.unmount();
    host.remove();
    Reflect.deleteProperty(Element.prototype, "scrollIntoView");
    window.history.replaceState(null, "", "/");
  });

  it("S1 A held ISBN with none of the reader's copies shows the pencil, none of the reader's copies", async () => {
    // Given
    const screen = open("/isbn", { isbn: theHouseHolds(onePiece2, []) });

    // When
    await ask(screen, "9782723489898");

    // Then
    await screen.findByRole("heading", {
      name: "Aux prises avec Baggy et ses hommes",
    });
    expect(screen.getByText("Dans aucune de vos bibliothèques")).toBeDefined();
    expect(pencil(screen)).toBeDefined();
  });

  it("S1 A held ISBN with none of the reader's copies shows the pencil, a copy of the reader's", async () => {
    // Given
    const screen = open("/isbn", {
      isbn: theHouseHolds(onePiece2, [on(lea.defaultBookshelf)]),
    });

    // When
    await ask(screen, "9782723489898");

    // Then
    await screen.findByRole("heading", {
      name: "Aux prises avec Baggy et ses hommes",
    });
    expect(rows(screen)).toEqual(["Dans Bibliothèque de Léa"]);
    expect(screen.queryByLabelText("Titre")).toBeNull();
    expect(screen.queryByRole("button", { name: "Modifier" })).toBeNull();
  });

  it("S2 A held ISBN is added as edited", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = open("/isbn", { isbn: theHouseHolds(onePiece2, []), add });
    await ask(screen, "9782723489898");

    // When
    await fireEvent.click(
      await screen.findByRole("button", { name: "Modifier" }),
    );

    // Then
    await screen.findByLabelText("Titre");
    expect(values(screen, "Tome")).toEqual({
      Titre: "Aux prises avec Baggy et ses hommes",
      "Sous-titre": "",
      Série: "One piece",
      Tome: "2",
      Collection: "Shonen manga",
      Éditeur: "Glénat",
      Année: "2013",
      Langue: "français",
      Pages: "208",
      Résumé: "",
    });

    // When
    await fill(screen, "Titre", "Luffy versus la bande à Baggy !!");
    await tapAdd(screen);

    // Then
    await screen.findByRole("heading", {
      name: "Luffy versus la bande à Baggy !!",
    });
    expect(add.asked).toEqual([
      {
        bookshelfId: lea.defaultBookshelf.id,
        edition: { ...onePiece2, title: "Luffy versus la bande à Baggy !!" },
        coverSource: null,
      },
    ]);
    expect(rows(screen)).toEqual(["Dans Bibliothèque de Léa"]);
    expect(screen.queryByLabelText("Titre")).toBeNull();
  });

  it("S3 The pencil opens the form on the ouvrage's page", async () => {
    // Given
    const screen = open(pageOf(asterix), { catalogue: librisAnswers(asterix) });
    await screen.findByRole("button", trashName);

    // When
    await fireEvent.click(pencil(screen));

    // Then
    await screen.findByLabelText("Titre");
    expect(values(screen, "Album")).toEqual({
      Titre: "Astérix le Gaulois",
      "Sous-titre": "",
      Série: "",
      Album: "",
      Collection: "",
      Éditeur: "Hachette",
      Année: "2004",
      Langue: "français",
      Pages: "48",
      Résumé: "",
    });
    expect(kind(screen, "BD").checked).toBe(true);
    expect(authors(screen)).toEqual([
      { name: "René Goscinny", roles: "scénario" },
      { name: "Albert Uderzo", roles: "dessin" },
    ]);
    expect(rows(screen)).toEqual(["Dans Bibliothèque de Léa"]);
    expect(screen.queryAllByRole("button", trashName)).toEqual([]);
    expect(cross(screen)).toBeDefined();
    expect(screen.queryByRole("button", { name: "Modifier" })).toBeNull();
    expect(saveButton(screen)).toBeDefined();
  });

  it("S4 The ouvrage is saved as edited", async () => {
    // Given
    const save = librisSaves({
      ...asterix,
      subtitle: "Une aventure d'Astérix",
      series: { name: "Astérix", volumeNumber: 1 },
    });
    const screen = await formOfThePage({ save });

    // When
    await fill(screen, "Sous-titre", "  Une aventure d'Astérix ");
    await fill(screen, "Série", " asterix ");
    await fill(screen, "Album", "1");
    await tapSave(screen);

    // Then
    await screen.findByText("Astérix · album 1");
    expect(save.asked).toEqual([
      {
        id: asterix.id,
        edition: {
          kind: "BD",
          title: "Astérix le Gaulois",
          subtitle: "Une aventure d'Astérix",
          authors: [
            { name: "René Goscinny", role: "WRITER" },
            { name: "Albert Uderzo", role: "ARTIST" },
          ],
          series: { name: "asterix", volumeNumber: 1 },
          collection: null,
          publisher: "Hachette",
          publicationYear: 2004,
          language: "fr",
          pageCount: 48,
          summary: null,
        },
      },
    ]);
    expect(text()).not.toContain("asterix");
    expect(rows(screen)).toEqual(["Dans Bibliothèque de Léa"]);
    expect(screen.queryByLabelText("Titre")).toBeNull();
    expect(screen.queryByRole("button", { name: "Enregistrer" })).toBeNull();
    expect(pencil(screen)).toBeDefined();
  });

  it("S8 The cross drops the edits", async () => {
    // Given
    const screen = await formOfThePage({});
    await fill(screen, "Titre", "Astérix le gaulois");

    // When
    await fireEvent.click(cross(screen));

    // Then
    await screen.findByRole("button", trashName);
    expect(screen.queryByLabelText("Titre")).toBeNull();
    expect(screen.queryByRole("button", { name: "Enregistrer" })).toBeNull();
    await fireEvent.click(pencil(screen));
    expect((await screen.findByLabelText<Field>("Titre")).value).toBe(
      "Astérix le Gaulois",
    );
  });

  it.each([
    {
      case: "the save answers the validation problem",
      save: () =>
        new FakeEditionsApi({
          outcome: "problem",
          type: "/problems/validation",
        }),
    },
    {
      case: "the save answers the not-found problem",
      save: () =>
        new FakeEditionsApi({
          outcome: "problem",
          type: "/problems/not-found",
        }),
    },
    {
      case: "the save fails",
      save: () => new FakeEditionsApi(new TypeError("Failed to fetch")),
    },
  ])("S9 A refused or failed save, $case", async ({ save }) => {
    // Given
    const screen = await formOfThePage({ save: save() });
    await fill(screen, "Titre", "Astérix le gaulois");

    // When
    await tapSave(screen);

    // Then
    await screen.findByText(
      "Erreur lors de l'enregistrement, veuillez réessayer plus tard.",
    );
    expect(field(screen, "Titre").value).toBe("Astérix le gaulois");
    expect(field(screen, "Titre").disabled).toBe(false);
    expect(saveButton(screen)).toBeDefined();
  });

  it("S9 A refused or failed save, the title emptied", async () => {
    // Given
    const save = librisSaves(asterix);
    const screen = await formOfThePage({ save });
    await fill(screen, "Titre", "");

    // When
    await tapSave(screen);

    // Then
    await screen.findByText("Titre manquant");
    expect(document.activeElement).toBe(field(screen, "Titre"));
    expect(save.asked).toEqual([]);
  });

  function theHouseHolds(edition: SourceEdition, copies: Copy[]): IsbnApi {
    return new FakeIsbnApi({
      outcome: "found",
      id: "8b3f4e5d-6c7a-4b8c-9d0e-1f2a3b4c5d6e",
      edition,
      copies,
      covers: [],
    });
  }

  function librisAnswers(book: Book): CatalogueApi {
    return new FakeCatalogueApi([], { outcome: "found", book });
  }

  function on(bookshelf: Bookshelf): Copy {
    return {
      id: "5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50",
      bookshelf: { ...bookshelf, role: "OWNER" },
    };
  }

  function librisAdds(copy: Copy): FakeBookshelfApi {
    return new FakeBookshelfApi({ outcome: "added", copy });
  }

  function librisSaves(book: Book): FakeEditionsApi {
    return new FakeEditionsApi({ outcome: "saved", book });
  }

  function pageOf(book: Book) {
    return `/catalogue/ouvrages/${book.id}`;
  }

  async function formOfThePage(world: { save?: EditionsApi }) {
    const screen = open(pageOf(asterix), {
      catalogue: librisAnswers(asterix),
      ...world,
    });
    await fireEvent.click(
      await screen.findByRole("button", { name: "Modifier" }),
    );
    await screen.findByLabelText("Titre");
    return screen;
  }

  function open(
    path: string,
    world: {
      isbn?: IsbnApi;
      add?: BookshelfApi;
      catalogue?: CatalogueApi;
      save?: EditionsApi;
    },
  ) {
    window.history.replaceState(null, "", path);
    app = createLibrisApp(
      {
        meApi: new FakeMeApi(lea),
        isbnApi:
          world.isbn ??
          new FakeIsbnApi({ outcome: "problem", type: "/problems/not-found" }),
        bookshelfApi:
          world.add ??
          new FakeBookshelfApi(new Error("no add in this scenario")),
        barcodeScanner: new FakeBarcodeScanner(false),
        appUpdate: new FakeAppUpdate(),
        catalogueApi: world.catalogue ?? new FakeCatalogueApi([]),
        bookshelvesApi: new FakeBookshelvesApi({ memberships: [] }),
        readersApi: new FakeReadersApi([]),
        seriesApi: new FakeSeriesApi([]),
        copiesApi: new FakeCopiesApi(new Error("no removal in this scenario")),
        beeper: new FakeBeeper(),
        beepSetting: new FakeBeepSetting(),
        authorsApi: new FakeAuthorsApi([]),
        editionsApi:
          world.save ??
          new FakeEditionsApi(new Error("no save in this scenario")),
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

  async function fill(screen: Screen, label: string, text: string) {
    const target = field(screen, label);
    target.focus();
    await fireEvent.input(target, { target: { value: text } });
  }

  function field(screen: Screen, label: string) {
    return screen.getByLabelText<Field>(label);
  }

  function values(screen: Screen, volume: "Tome" | "Album") {
    return Object.fromEntries(
      [
        "Titre",
        "Sous-titre",
        "Série",
        volume,
        "Collection",
        "Éditeur",
        "Année",
        "Langue",
        "Pages",
        "Résumé",
      ].map((label) => [label, field(screen, label).value]),
    );
  }

  function kind(screen: Screen, name: string) {
    return screen.getByRole<HTMLInputElement>("radio", { name });
  }

  function authors(screen: Screen) {
    const lines = screen
      .queryAllByRole("button")
      .filter((button) => button.hasAttribute("aria-expanded"));
    return screen
      .queryAllByLabelText<HTMLInputElement>("Nom de l'auteur")
      .map((box, index) => ({
        name: box.value,
        roles: lines[index]?.textContent?.trim(),
      }));
  }

  function rows(screen: Screen) {
    return screen
      .queryAllByText(/^Dans /)
      .map((row) => row.textContent?.trim() ?? "");
  }

  function pencil(screen: Screen) {
    return screen.getByRole("button", { name: "Modifier" });
  }

  function cross(screen: Screen) {
    return screen.getByRole("button", { name: "Annuler les modifications" });
  }

  function saveButton(screen: Screen) {
    return screen.getByRole("button", { name: "Enregistrer" });
  }

  async function tapAdd(screen: Screen) {
    const button = screen.getByRole("button", {
      name: "Ajouter à ma bibliothèque",
    });
    button.focus();
    await fireEvent.click(button);
  }

  async function tapSave(screen: Screen) {
    const button = saveButton(screen);
    button.focus();
    await fireEvent.click(button);
  }

  function text() {
    return host.textContent ?? "";
  }
});
