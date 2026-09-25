import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, it } from "vitest";
import type { App } from "vue";
import type { BookshelfApi } from "../application/BookshelfApi";
import type { IsbnApi } from "../application/IsbnApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Copy } from "../domain/Copy";
import type { SourceEdition } from "../domain/SourceEdition";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import { FakeBarcodeScanner } from "../fixture/FakeBarcodeScanner";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeIsbnApi } from "../fixture/FakeIsbnApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { lea } from "../fixture/Readers";
import { onePiece1, onePiece2 } from "../fixture/SourceEditions";

type Screen = BoundFunctions<typeof queries>;

const salon: Bookshelf = {
  id: "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f",
  name: "Salon",
};

describe("Bookshelf", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
  });

  it("S4 The ouvrage is already in a bookshelf", async () => {
    // Given
    const screen = open("/isbn", {
      isbn: theHouseHolds(onePiece2, [
        on(lea.defaultBookshelf, "6f1d2c3b-4a59-4e6f-8b70-1c2d3e4f5a61"),
        on(salon, "7a2e3d4c-5b6a-4f70-9c81-2d3e4f5a6b72"),
      ]),
    });

    // When
    await ask(screen, "9782723489898");

    // Then
    await screen.findByText("Aux prises avec Baggy et ses hommes");
    expect(rows(screen)).toEqual(["Dans Bibliothèque de Léa", "Dans Salon"]);
    expect(card()).not.toContain("exemplaires");
  });

  it("S4 The ouvrage is already in a bookshelf, none of the reader's", async () => {
    // Given
    const screen = open("/isbn", { isbn: theHouseHolds(onePiece2, []) });

    // When
    await ask(screen, "9782723489898");

    // Then
    await screen.findByText("Aux prises avec Baggy et ses hommes");
    expect(rows(screen)).toEqual(["Dans aucune de vos bibliothèques"]);
  });

  it("S2 The ouvrage is added", async () => {
    // Given
    const screen = open("/isbn", {
      isbn: aSourceKnows(onePiece1),
      add: librisAdds(
        on(lea.defaultBookshelf, "5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50"),
      ),
    });
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");
    expect(rows(screen)).toEqual(["Dans aucune de vos bibliothèques"]);

    // When
    await fireEvent.click(addButton(screen));

    // Then
    await screen.findByText("Dans Bibliothèque de Léa");
    expect(rows(screen)).toEqual(["Dans Bibliothèque de Léa"]);
    expect(
      screen.queryByRole("button", { name: "Ajouter à ma bibliothèque" }),
    ).toBeNull();
  });

  it.skip("S5 Libris unavailable during the add", async () => {
    // Given
    const screen = open("/isbn", {
      isbn: aSourceKnows(onePiece1),
      add: librisDoesNotAnswerTheAdd(),
    });
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");

    // When
    await fireEvent.click(addButton(screen));

    // Then
    await screen.findByText(
      "Erreur lors de l'ajout, veuillez réessayer plus tard.",
    );
    expect(rows(screen)).toEqual(["Dans aucune de vos bibliothèques"]);
    expect(addButton(screen)).toBeDefined();
  });

  function theHouseHolds(edition: SourceEdition, copies: Copy[]): IsbnApi {
    return new FakeIsbnApi({ outcome: "found", edition, copies });
  }

  function aSourceKnows(edition: SourceEdition): IsbnApi {
    return new FakeIsbnApi({ outcome: "found", edition, copies: [] });
  }

  function on(bookshelf: Bookshelf, copyId: string): Copy {
    return { id: copyId, bookshelf };
  }

  function librisAdds(copy: Copy): BookshelfApi {
    return new FakeBookshelfApi({ outcome: "added", copy });
  }

  function librisDoesNotAnswerTheAdd(): BookshelfApi {
    return new FakeBookshelfApi(new TypeError("Failed to fetch"));
  }

  function open(path: string, world: { isbn: IsbnApi; add?: BookshelfApi }) {
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

  function rows(screen: Screen) {
    return screen
      .queryAllByText(/^Dans /)
      .map((row) => row.textContent?.trim() ?? "");
  }

  function card() {
    return host.textContent ?? "";
  }
});
