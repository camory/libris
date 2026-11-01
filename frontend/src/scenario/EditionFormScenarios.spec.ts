import {
  fireEvent,
  queries,
  waitFor,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { flushPromises } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import type { App } from "vue";
import type { BookshelfApi } from "../application/BookshelfApi";
import type { IsbnApi } from "../application/IsbnApi";
import type { SeriesApi } from "../application/SeriesApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Copy } from "../domain/Copy";
import type { CoverCandidate } from "../domain/Cover";
import type { SourceEdition } from "../domain/SourceEdition";
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
import { onePiece1, onePiece2 } from "../fixture/SourceEditions";

type Screen = BoundFunctions<typeof queries>;
type Field = HTMLInputElement | HTMLTextAreaElement;

const inventaire: CoverCandidate = {
  source: "inventaire.io",
  url: "https://inventaire.io/img/entities/480x600/34d6e7d99cec5b0922b9eccfeb03748ab2b4db99",
};

const openLibrary: CoverCandidate = {
  source: "Open Library",
  url: "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg?default=false",
};

const houseSeries = [
  "Pierrot",
  "Spirou et Fantasio",
  "Piège",
  "One piece",
  "Les Pieds Nickelés",
  "Astérix",
  "Épiés",
  "Pièces détachées",
];

const fieldLabels = [
  "Titre",
  "Sous-titre",
  "Série",
  "Tome",
  "Collection",
  "Éditeur",
  "Année",
  "Langue",
  "Pages",
  "Résumé",
];

describe("Edition form", () => {
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

  it.skip("S1 A new ISBN opens the form", async () => {
    // Given
    const screen = open({
      isbn: aSourceKnows(onePiece1, [inventaire, openLibrary]),
    });

    // When
    await ask(screen, "9782723488525");

    // Then
    await screen.findByLabelText("Titre");
    expect(screen.queryByRole("heading", { name: "Romance dawn" })).toBeNull();
    expect(values(screen)).toEqual({
      Titre: "Romance dawn",
      "Sous-titre": "à l'aube d'une grande aventure",
      Série: "One piece",
      Tome: "1",
      Collection: "Shonen manga",
      Éditeur: "Glénat",
      Année: "2013",
      Langue: "français",
      Pages: "203",
      Résumé: "",
    });
    expect(kind(screen, "Manga").checked).toBe(true);
    expect(text()).toContain("Eiichirō Oda");
    expect(text()).toContain("9782723488525");
    expect(coverImage(screen, "Romance dawn").getAttribute("src")).toBe(
      inventaire.url,
    );
    expect(text()).toContain("inventaire.io");
    expect(dots(screen)).toEqual([
      "Couverture inventaire.io",
      "Couverture Open Library",
    ]);
  });

  it.skip("S2 An unknown ISBN opens an empty form", async () => {
    // Given
    const screen = open({ isbn: noSourceKnows() });

    // When
    await ask(screen, "9782000000006");

    // Then
    await screen.findByLabelText("Titre");
    expect(text()).toContain("9782000000006");
    expect(Object.values(values(screen))).toEqual(fieldLabels.map(() => ""));
    expect(kind(screen, "Livre").checked).toBe(true);
    expect(screen.queryByRole("img")).toBeNull();
    expect(screen.queryByText("ISBN inconnu")).toBeNull();
  });

  it("S3 Every source down shows no form", async () => {
    // Given
    const screen = open({ isbn: theSourcesAreDown() });

    // When
    await ask(screen, "9791000000008");

    // Then
    await screen.findByText(
      "Erreur lors de la recherche, veuillez réessayer plus tard.",
    );
    expect(screen.queryByLabelText("Titre")).toBeNull();
  });

  it("S4 A held ISBN shows its card", async () => {
    // Given
    const screen = open({
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
  });

  it.skip("S5 The ouvrage is added as edited", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = open({ isbn: aSourceKnows(onePiece1), add });
    await ask(screen, "9782723488525");
    await screen.findByLabelText("Titre");

    // When
    await fill(screen, "Titre", "  Romance Dawn  ");
    await fill(screen, "Sous-titre", "   ");
    await fill(screen, "Série", "One Piece ");
    await tapAdd(screen);

    // Then
    await screen.findByRole("heading", { name: "Romance Dawn" });
    expect(add.asked).toEqual([
      {
        bookshelfId: lea.defaultBookshelf.id,
        edition: {
          ...onePiece1,
          title: "Romance Dawn",
          subtitle: null,
          series: { name: "One Piece", volumeNumber: 1 },
        },
        coverSource: null,
      },
    ]);
    expect(text()).toContain("One Piece");
    expect(text()).not.toContain("à l'aube d'une grande aventure");
    expect(rows(screen)).toEqual(["Dans Bibliothèque de Léa"]);
    expect(screen.queryByLabelText("Titre")).toBeNull();
    expect(
      screen.queryByRole("button", { name: "Ajouter à ma bibliothèque" }),
    ).toBeNull();
  });

  it.skip("S7 The series of the house are suggested, the suggestions", async () => {
    // Given
    const screen = await formOfOnePiece1({
      names: theHouseHoldsTheSeries(...houseSeries),
    });

    // When
    await fill(screen, "Série", " PIE ");

    // Then
    await waitFor(() =>
      expect(suggestions(screen)).toEqual([
        "Épiés",
        "Les Pieds Nickelés",
        "One piece",
        "Pièces détachées",
        "Piège",
      ]),
    );
    expect(field(screen, "Série").value).toBe(" PIE ");
  });

  it.skip("S7 The series of the house are suggested, a tap", async () => {
    // Given
    const screen = await formOfOnePiece1({
      names: theHouseHoldsTheSeries(...houseSeries),
    });
    await fill(screen, "Série", "one pi");
    await waitFor(() => expect(suggestions(screen)).toEqual(["One piece"]));

    // When
    await fireEvent.click(screen.getByRole("button", { name: "One piece" }));

    // Then
    expect(field(screen, "Série").value).toBe("One piece");
    expect(suggestions(screen)).toEqual([]);
    await screen.findByText("Existante");
  });

  it.skip("S7 The series of the house are suggested, Existante", async () => {
    // Given
    const screen = await formOfOnePiece1({
      names: theHouseHoldsTheSeries(...houseSeries),
    });

    // When
    await fill(screen, "Série", " one PIÉCE ");

    // Then
    await screen.findByText("Existante");
    expect(screen.queryByText("Nouvelle")).toBeNull();
    expect(field(screen, "Série").value).toBe(" one PIÉCE ");
  });

  it.skip("S7 The series of the house are suggested, Nouvelle", async () => {
    // Given
    const screen = await formOfOnePiece1({
      names: theHouseHoldsTheSeries(...houseSeries),
    });

    // When
    await fill(screen, "Série", "One pie");

    // Then
    await screen.findByText("Nouvelle");
    expect(screen.queryByText("Existante")).toBeNull();
  });

  it.skip("S8 No suggestions when the names fail", async () => {
    // Given
    const screen = await formOfOnePiece1({
      names: theSeriesNamesAreUnavailable(),
    });

    // When
    await fill(screen, "Série", "One pie");
    await flushPromises();

    // Then
    expect(suggestions(screen)).toEqual([]);
    expect(screen.queryByText("Existante")).toBeNull();
    expect(screen.queryByText("Nouvelle")).toBeNull();
    expect(field(screen, "Série").value).toBe("One pie");
  });

  it.skip.each([
    {
      case: "an empty title",
      label: "Titre",
      typed: "",
      says: "Titre manquant",
    },
    {
      case: "a blank title",
      label: "Titre",
      typed: "   ",
      says: "Titre manquant",
    },
    {
      case: "a volume number of 0",
      label: "Tome",
      typed: "0",
      says: "Nombre invalide",
    },
    {
      case: "a year in words",
      label: "Année",
      typed: "deux mille",
      says: "Nombre invalide",
    },
    {
      case: "a page count with a decimal",
      label: "Pages",
      typed: "1,5",
      says: "Nombre invalide",
    },
    {
      case: "a word that names no language",
      label: "Langue",
      typed: "elfique",
      says: "Langue inconnue",
    },
  ])("S10 A refused form, $case", async ({ label, typed, says }) => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOfOnePiece1({ add });
    await fill(screen, label, typed);

    // When
    await tapAdd(screen);

    // Then
    await screen.findByText(says);
    expect(isRefused(field(screen, label))).toBe(true);
    expect(document.activeElement).toBe(field(screen, label));
    expect(add.asked).toEqual([]);
  });

  it.skip("S10 A refused form, Tome sans série", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOfOnePiece1({ add });
    await fill(screen, "Série", "");

    // When
    await tapAdd(screen);

    // Then
    await screen.findByText("Tome sans série");
    expect(isRefused(field(screen, "Tome"))).toBe(true);
    expect(document.activeElement).toBe(field(screen, "Tome"));
    expect(add.asked).toEqual([]);
  });

  it.skip("S10 A refused form, Album sans série", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOfOnePiece1({ add });
    await fireEvent.click(kind(screen, "BD"));
    await fill(screen, "Série", "");

    // When
    await tapAdd(screen);

    // Then
    await screen.findByText("Album sans série");
    expect(isRefused(field(screen, "Album"))).toBe(true);
    expect(document.activeElement).toBe(field(screen, "Album"));
    expect(add.asked).toEqual([]);
  });

  it.skip("S10 A refused form, two fields refused", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOfOnePiece1({ add });
    await fill(screen, "Titre", "");
    await fill(screen, "Série", "");

    // When
    await tapAdd(screen);

    // Then
    await screen.findByText("Titre manquant");
    expect(screen.getByText("Tome sans série")).toBeDefined();
    expect(isRefused(field(screen, "Titre"))).toBe(true);
    expect(isRefused(field(screen, "Tome"))).toBe(true);
    expect(document.activeElement).toBe(field(screen, "Titre"));
    expect(add.asked).toEqual([]);
  });

  it.skip.each([
    {
      case: "the add answers the validation problem",
      add: () =>
        new FakeBookshelfApi({
          outcome: "problem",
          type: "/problems/validation",
        }),
    },
    {
      case: "the add fails",
      add: () => new FakeBookshelfApi(new TypeError("Failed to fetch")),
    },
  ])("S11 Libris refuses or fails the add, $case", async ({ add }) => {
    // Given
    const screen = await formOfOnePiece1({ add: add() });
    await fill(screen, "Titre", "Romance Dawn");

    // When
    await tapAdd(screen);

    // Then
    await screen.findByText(
      "Erreur lors de l'ajout, veuillez réessayer plus tard.",
    );
    expect(field(screen, "Titre").value).toBe("Romance Dawn");
    expect(field(screen, "Titre").disabled).toBe(false);
    expect(addButton(screen)).toBeDefined();
  });

  function aSourceKnows(
    edition: SourceEdition,
    covers: CoverCandidate[] = [],
  ): IsbnApi {
    return new FakeIsbnApi({
      outcome: "found",
      id: null,
      edition,
      copies: [],
      covers,
    });
  }

  function theHouseHolds(edition: SourceEdition, copies: Copy[]): IsbnApi {
    return new FakeIsbnApi({
      outcome: "found",
      id: "8b3f4e5d-6c7a-4b8c-9d0e-1f2a3b4c5d6e",
      edition,
      copies,
      covers: [],
    });
  }

  function noSourceKnows(): IsbnApi {
    return new FakeIsbnApi({ outcome: "problem", type: "/problems/not-found" });
  }

  function theSourcesAreDown(): IsbnApi {
    return new FakeIsbnApi({
      outcome: "problem",
      type: "/problems/sources-unavailable",
    });
  }

  function theHouseHoldsTheSeries(...names: string[]): SeriesApi {
    return new FakeSeriesApi([], undefined, names);
  }

  function theSeriesNamesAreUnavailable(): SeriesApi {
    return new FakeSeriesApi([], undefined, new TypeError("Failed to fetch"));
  }

  function on(bookshelf: Bookshelf): Copy {
    return { id: "5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50", bookshelf };
  }

  function librisAdds(copy: Copy): FakeBookshelfApi {
    return new FakeBookshelfApi({ outcome: "added", copy });
  }

  async function formOfOnePiece1(world: {
    add?: BookshelfApi;
    names?: SeriesApi;
  }) {
    const screen = open({ isbn: aSourceKnows(onePiece1), ...world });
    await ask(screen, "9782723488525");
    await screen.findByLabelText("Titre");
    return screen;
  }

  function open(world: {
    isbn: IsbnApi;
    add?: BookshelfApi;
    names?: SeriesApi;
  }) {
    window.history.replaceState(null, "", "/isbn");
    app = createLibrisApp(
      {
        meApi: new FakeMeApi(lea),
        isbnApi: world.isbn,
        bookshelfApi:
          world.add ??
          new FakeBookshelfApi(new Error("no add in this scenario")),
        barcodeScanner: new FakeBarcodeScanner(false),
        appUpdate: new FakeAppUpdate(),
        catalogueApi: new FakeCatalogueApi([]),
        bookshelvesApi: new FakeBookshelvesApi({ memberships: [] }),
        readersApi: new FakeReadersApi([]),
        seriesApi: world.names ?? new FakeSeriesApi([]),
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

  function values(screen: Screen) {
    return Object.fromEntries(
      fieldLabels.map((label) => [label, field(screen, label).value]),
    );
  }

  function kind(screen: Screen, name: string) {
    return screen.getByRole<HTMLInputElement>("radio", { name });
  }

  function isRefused(target: Field) {
    return target.getAttribute("aria-invalid") === "true";
  }

  function suggestions(screen: Screen) {
    return screen
      .queryAllByRole("button")
      .map((button) => button.textContent?.trim() ?? "")
      .filter((name) => houseSeries.includes(name));
  }

  async function tapAdd(screen: Screen) {
    const button = addButton(screen);
    button.focus();
    await fireEvent.click(button);
  }

  function addButton(screen: Screen) {
    return screen.getByRole("button", { name: "Ajouter à ma bibliothèque" });
  }

  function coverImage(screen: Screen, title: string) {
    return screen.getByRole("img", { name: `Couverture de ${title}` });
  }

  function dots(screen: Screen) {
    return screen
      .getAllByRole("button", { name: /^Couverture / })
      .map((dot) => dot.getAttribute("aria-label"));
  }

  function rows(screen: Screen) {
    return screen
      .queryAllByText(/^Dans /)
      .map((row) => row.textContent?.trim() ?? "");
  }

  function text() {
    return host.textContent ?? "";
  }
});
