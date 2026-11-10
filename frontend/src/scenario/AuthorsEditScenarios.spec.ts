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
import type { AuthorsApi } from "../application/AuthorsApi";
import type { BookshelfApi } from "../application/BookshelfApi";
import type { IsbnApi } from "../application/IsbnApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Copy } from "../domain/Copy";
import type { SourceEdition } from "../domain/SourceEdition";
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
import { onePiece1 } from "../fixture/SourceEditions";

type Screen = BoundFunctions<typeof queries>;

const asterix1: SourceEdition = {
  isbn13: "9782012101333",
  kind: "BD",
  title: "Astérix le Gaulois",
  subtitle: null,
  authors: [
    { name: "René Goscinny", role: "WRITER" },
    { name: "Albert Uderzo", role: "ARTIST" },
  ],
  series: { name: "Astérix", volumeNumber: 1 },
  collection: null,
  publisher: "Hachette",
  publicationYear: 2004,
  language: "fr",
  pageCount: 48,
  summary: null,
};

const houseAuthors = [
  "René Goscinny",
  "Albert Uderzo",
  "Pierre Christin",
  "Hergé",
  "Jean-Claude Mézières",
  "André Franquin",
  "Éric Hérenguel",
  "Florence Cestac",
  "Régis Loisel",
];

describe("Authors edit", () => {
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

  it.each([
    {
      case: "one author with two roles",
      edition: onePiece1,
      rows: [{ name: "Eiichirō Oda", roles: "scénario, dessin" }],
    },
    {
      case: "two authors",
      edition: asterix1,
      rows: [
        { name: "René Goscinny", roles: "scénario" },
        { name: "Albert Uderzo", roles: "dessin" },
      ],
    },
  ])("S1 The authors open as rows, $case", async ({ edition, rows }) => {
    // Given
    const screen = open({ isbn: aSourceKnows(edition) });
    await ask(screen, edition.isbn13);

    // When
    await fireEvent.click(
      await screen.findByRole("button", { name: "Modifier" }),
    );

    // Then
    await screen.findByLabelText("Titre");
    expect(authors(screen)).toEqual(rows);
  });

  it("S2 An author is added", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = open({ isbn: noSourceKnows(), add });
    await ask(screen, "9782000000006");

    // When
    await screen.findByLabelText("Titre");

    // Then
    expect(authors(screen)).toEqual([]);
    expect(addAuthor(screen)).toBeDefined();

    // When
    await fireEvent.click(addAuthor(screen));

    // Then
    expect(authors(screen)).toEqual([{ name: "", roles: "texte" }]);
    await waitFor(() => expect(document.activeElement).toBe(names(screen)[0]));

    // When
    await fill(screen.getByLabelText("Titre"), "Les Misérables");
    await fill(names(screen)[0], "Victor Hugo");
    await tapAdd(screen);

    // Then
    await screen.findByRole("heading", { name: "Les Misérables" });
    expect(sentAuthors(add)).toEqual([
      [{ name: "Victor Hugo", role: "WRITER" }],
    ]);
  });

  it("S2 An author is added, after the others", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOf(asterix1, { add });

    // When
    await fireEvent.click(addAuthor(screen));

    // Then
    expect(authors(screen)).toEqual([
      { name: "René Goscinny", roles: "scénario" },
      { name: "Albert Uderzo", roles: "dessin" },
      { name: "", roles: "scénario" },
    ]);
    await waitFor(() => expect(document.activeElement).toBe(names(screen)[2]));

    // When
    await fill(names(screen)[2], "Jean-Yves Ferri");
    await tapAdd(screen);

    // Then
    await screen.findByRole("heading", { name: "Astérix le Gaulois" });
    expect(sentAuthors(add)).toEqual([
      [
        { name: "René Goscinny", role: "WRITER" },
        { name: "Albert Uderzo", role: "ARTIST" },
        { name: "Jean-Yves Ferri", role: "WRITER" },
      ],
    ]);
  });

  it("S3 An author is removed", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOf(asterix1, { add });

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "Retirer Albert Uderzo" }),
    );

    // Then
    expect(authors(screen)).toEqual([
      { name: "René Goscinny", roles: "scénario" },
    ]);
    await waitFor(() => expect(document.activeElement).toBe(addAuthor(screen)));

    // When
    await tapAdd(screen);

    // Then
    await screen.findByRole("heading", { name: "Astérix le Gaulois" });
    expect(sentAuthors(add)).toEqual([
      [{ name: "René Goscinny", role: "WRITER" }],
    ]);
  });

  it("S4 A name and its roles are changed", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOf(onePiece1, { add });

    // When
    await fill(names(screen)[0], "Eiichiro Oda");
    await fireEvent.click(rolesLines(screen)[0]);
    await fireEvent.click(role(screen, "dessin"));
    await fireEvent.click(role(screen, "traduction"));
    await tapAdd(screen);

    // Then
    await screen.findByRole("heading", { name: "Romance dawn" });
    expect(sentAuthors(add)).toEqual([
      [
        { name: "Eiichiro Oda", role: "WRITER" },
        { name: "Eiichiro Oda", role: "TRANSLATOR" },
      ],
    ]);
  });

  it("S5 An author of the house is matched by name", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOf(onePiece1, {
      add,
      authors: theHouseHoldsTheAuthors("Eiichirō Oda"),
    });

    // When
    await fill(names(screen)[0], "  eiichiro oda  ");
    await tapAdd(screen);

    // Then
    await screen.findByRole("heading", { name: "Romance dawn" });
    expect(sentAuthors(add)).toEqual([
      [
        { name: "eiichiro oda", role: "WRITER" },
        { name: "eiichiro oda", role: "ARTIST" },
      ],
    ]);
  });

  it("S6 The authors of the house are suggested, the suggestions", async () => {
    // Given
    const screen = await formOf(onePiece1, {
      authors: theHouseHoldsTheAuthors(...houseAuthors),
    });

    // When
    await fill(names(screen)[0], " RE ");

    // Then
    await waitFor(() =>
      expect(suggestions(screen)).toEqual([
        "André Franquin",
        "Éric Hérenguel",
        "Florence Cestac",
        "Jean-Claude Mézières",
        "Pierre Christin",
      ]),
    );
    expect(names(screen)[0].value).toBe(" RE ");
  });

  it("S6 The authors of the house are suggested, a tap", async () => {
    // Given
    const screen = await formOf(onePiece1, {
      authors: theHouseHoldsTheAuthors(...houseAuthors),
    });
    await fill(names(screen)[0], "rené gos");
    await waitFor(() => expect(suggestions(screen)).toEqual(["René Goscinny"]));

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "René Goscinny" }),
    );

    // Then
    expect(names(screen)[0].value).toBe("René Goscinny");
    expect(suggestions(screen)).toEqual([]);
    await screen.findByText("Existant");
  });

  it("S6 The authors of the house are suggested, Existant", async () => {
    // Given
    const screen = await formOf(onePiece1, {
      authors: theHouseHoldsTheAuthors(...houseAuthors),
    });

    // When
    await fill(names(screen)[0], " rené GOSCINNY ");

    // Then
    await screen.findByText("Existant");
    expect(screen.queryByText("Nouveau")).toBeNull();
    expect(names(screen)[0].value).toBe(" rené GOSCINNY ");
  });

  it("S6 The authors of the house are suggested, Nouveau", async () => {
    // Given
    const screen = await formOf(onePiece1, {
      authors: theHouseHoldsTheAuthors(...houseAuthors),
    });

    // When
    await fill(names(screen)[0], "René Gosc");

    // Then
    await screen.findByText("Nouveau");
    expect(screen.queryByText("Existant")).toBeNull();
  });

  it("S7 No suggestions when the names fail", async () => {
    // Given
    const screen = await formOf(onePiece1, {
      authors: theAuthorNamesAreUnavailable(),
    });

    // When
    await fill(names(screen)[0], "René Gosc");
    await flushPromises();

    // Then
    expect(suggestions(screen)).toEqual([]);
    expect(screen.queryByText("Existant")).toBeNull();
    expect(screen.queryByText("Nouveau")).toBeNull();
    expect(names(screen)[0].value).toBe("René Gosc");
  });

  it("S8 A refused author, Rôle manquant", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOf(asterix1, { add });
    await fireEvent.click(rolesLines(screen)[1]);
    await fireEvent.click(role(screen, "dessin"));

    // When
    await tapAdd(screen);

    // Then
    await screen.findByText("Rôle manquant");
    expect(document.activeElement).toBe(rolesLines(screen)[1]);
    expect(add.asked).toEqual([]);
  });

  it("S8 A refused author, Auteur en double", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOf(asterix1, { add });
    await fireEvent.click(addAuthor(screen));
    await fill(names(screen)[2], " rené GOSCINNY ");
    await fireEvent.click(rolesLines(screen)[2]);
    await fireEvent.click(role(screen, "scénario"));
    await fireEvent.click(role(screen, "dessin"));

    // When
    await tapAdd(screen);

    // Then
    await screen.findByText("Auteur en double");
    expect(document.activeElement).toBe(names(screen)[2]);
    expect(add.asked).toEqual([]);
  });

  it("S9 An empty row is dropped, an empty name", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOf(onePiece1, { add });
    await fireEvent.click(addAuthor(screen));

    // When
    await tapAdd(screen);

    // Then
    await screen.findByRole("heading", { name: "Romance dawn" });
    expect(sentAuthors(add)).toEqual([onePiece1.authors]);
  });

  it("S9 An empty row is dropped, spaces alone and no role", async () => {
    // Given
    const add = librisAdds(on(lea.defaultBookshelf));
    const screen = await formOf(onePiece1, { add });
    await fireEvent.click(addAuthor(screen));
    await fill(names(screen)[1], "   ");
    await fireEvent.click(rolesLines(screen)[1]);
    await fireEvent.click(role(screen, "scénario"));

    // When
    await tapAdd(screen);

    // Then
    await screen.findByRole("heading", { name: "Romance dawn" });
    expect(sentAuthors(add)).toEqual([onePiece1.authors]);
  });

  function aSourceKnows(edition: SourceEdition): IsbnApi {
    return new FakeIsbnApi({
      outcome: "found",
      id: null,
      edition,
      copies: [],
      covers: [],
    });
  }

  function noSourceKnows(): IsbnApi {
    return new FakeIsbnApi({ outcome: "problem", type: "/problems/not-found" });
  }

  function theHouseHoldsTheAuthors(...authorNames: string[]): AuthorsApi {
    return new FakeAuthorsApi(authorNames);
  }

  function theAuthorNamesAreUnavailable(): AuthorsApi {
    return new FakeAuthorsApi(new TypeError("Failed to fetch"));
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

  async function formOf(
    edition: SourceEdition,
    world: { add?: BookshelfApi; authors?: AuthorsApi },
  ) {
    const screen = open({ isbn: aSourceKnows(edition), ...world });
    await ask(screen, edition.isbn13);
    await fireEvent.click(
      await screen.findByRole("button", { name: "Modifier" }),
    );
    await screen.findByLabelText("Titre");
    return screen;
  }

  function open(world: {
    isbn: IsbnApi;
    add?: BookshelfApi;
    authors?: AuthorsApi;
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
        seriesApi: new FakeSeriesApi([]),
        copiesApi: new FakeCopiesApi(new Error("no removal in this scenario")),
        beeper: new FakeBeeper(),
        beepSetting: new FakeBeepSetting(),
        authorsApi: world.authors ?? new FakeAuthorsApi([]),
        editionsApi: new FakeEditionsApi(new Error("no save in this scenario")),
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

  async function fill(box: HTMLElement, text: string) {
    box.focus();
    await fireEvent.input(box, { target: { value: text } });
  }

  function names(screen: Screen) {
    return screen.queryAllByLabelText<HTMLInputElement>("Nom de l'auteur");
  }

  function rolesLines(screen: Screen) {
    return screen
      .queryAllByRole("button")
      .filter((button) => button.hasAttribute("aria-expanded"));
  }

  function authors(screen: Screen) {
    const lines = rolesLines(screen);
    return names(screen).map((box, index) => ({
      name: box.value,
      roles: lines[index]?.textContent?.trim(),
    }));
  }

  function role(screen: Screen, word: string) {
    return within(screen.getByRole("group", { name: "Rôles" })).getByRole(
      "button",
      { name: word },
    );
  }

  function suggestions(screen: Screen) {
    return screen
      .queryAllByRole("button")
      .map((button) => button.textContent?.trim() ?? "")
      .filter((name) => houseAuthors.includes(name));
  }

  function addAuthor(screen: Screen) {
    return screen.getByRole("button", { name: "Ajouter un auteur" });
  }

  async function tapAdd(screen: Screen) {
    const button = screen.getByRole("button", {
      name: "Ajouter à ma bibliothèque",
    });
    button.focus();
    await fireEvent.click(button);
  }

  function sentAuthors(add: FakeBookshelfApi) {
    return add.asked.map(({ edition }) => edition.authors);
  }
});
