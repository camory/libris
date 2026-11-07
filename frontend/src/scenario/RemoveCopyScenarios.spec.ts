import {
  fireEvent,
  queries,
  waitFor,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { App } from "vue";
import type { BookshelfApi } from "../application/BookshelfApi";
import type { CatalogueApi, FindBookAnswer } from "../application/CatalogueApi";
import { copiesApiKey, type CopiesApi } from "../application/CopiesApi";
import type { IsbnApi } from "../application/IsbnApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Book } from "../domain/Book";
import type { Copy, ReaderBookshelf } from "../domain/Copy";
import type { SourceEdition } from "../domain/SourceEdition";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import { asterixLeGaulois, bibliothequeDeLea, salon } from "../fixture/Books";
import { FakeBarcodeScanner } from "../fixture/FakeBarcodeScanner";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeBookshelvesApi } from "../fixture/FakeBookshelvesApi";
import { FakeCatalogueApi } from "../fixture/FakeCatalogueApi";
import { FakeCopiesApi } from "../fixture/FakeCopiesApi";
import { FakeIsbnApi } from "../fixture/FakeIsbnApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { FakeReadersApi } from "../fixture/FakeReadersApi";
import { FakeSeriesApi } from "../fixture/FakeSeriesApi";
import { lea } from "../fixture/Readers";
import { onePiece1, onePiece2 } from "../fixture/SourceEditions";

type Screen = BoundFunctions<typeof queries>;

const owned = { ...bibliothequeDeLea, role: "OWNER" as const };

const ownedToo = {
  id: "2d3e4f5a-6b7c-4d8e-9f0a-1b2c3d4e5f60",
  name: "Chambre",
  role: "OWNER" as const,
};

const joined = { ...salon, role: "VIEWER" as const };

const trashName = { name: "Retirer un exemplaire" };

const page = `/catalogue/ouvrages/${asterixLeGaulois.id}`;

describe("Remove a copy", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
    vi.unstubAllGlobals();
  });

  it.skip("S1 The owner removes one copy of two", async () => {
    // Given
    const first = on(owned, "3a4b5c6d-7e8f-4a9b-8c0d-1e2f3a4b5c6d");
    const second = on(owned, "4b5c6d7e-8f9a-4b0c-9d1e-2f3a4b5c6d7e");
    const removal = librisRemoves();
    const screen = open(page, {
      catalogue: librisAnswers(
        found(ouvrage(first, second)),
        found(ouvrage(second)),
      ),
      copies: removal,
    });
    await screen.findByText("2 exemplaires");

    // When
    await fireEvent.click(trash(screen));

    // Then
    await screen.findByText("Retirer un exemplaire de Bibliothèque de Léa ?");

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Retirer" }));

    // Then
    await waitFor(() => expect(screen.queryByText("2 exemplaires")).toBeNull());
    await screen.findByText("Dans Bibliothèque de Léa");
    expect(removal.asked).toHaveLength(1);
    expect([first.id, second.id]).toContain(removal.asked[0]);
  });

  it.skip("S2 The reader removes their last copy, opened from the catalogue", async () => {
    // Given
    const last = on(owned, "5c6d7e8f-9a0b-4c1d-8e2f-3a4b5c6d7e8f");
    const removal = librisRemoves();
    const screen = open("/catalogue", {
      catalogue: new FakeCatalogueApi(
        [
          { books: [ouvrage(last)], next: null },
          { books: [], next: null },
        ],
        [found(ouvrage(last)), notFound()],
      ),
      copies: removal,
    });
    await fireEvent.click(
      await screen.findByRole("link", { name: /Astérix le Gaulois/ }),
    );
    await screen.findByText("Dans Bibliothèque de Léa");

    // When
    await remove(screen);

    // Then
    expect(removal.asked).toEqual([last.id]);
    await waitFor(() => expect(window.location.pathname).toBe("/catalogue"));
  });

  it.skip("S2 The reader removes their last copy, opened from its address alone", async () => {
    // Given
    const last = on(owned, "6d7e8f9a-0b1c-4d2e-9f3a-4b5c6d7e8f9a");
    const removal = librisRemoves();
    const screen = open(page, {
      catalogue: new FakeCatalogueApi(
        [{ books: [], next: null }],
        [found(ouvrage(last)), notFound()],
      ),
      copies: removal,
    });
    await screen.findByText("Dans Bibliothèque de Léa");

    // When
    await remove(screen);

    // Then
    expect(removal.asked).toEqual([last.id]);
    await waitFor(() => expect(window.location.pathname).toBe("/catalogue"));
  });

  it("S3 A guest's row has no trash", async () => {
    // Given
    const catalogue = librisAnswers(
      found(
        ouvrage(
          on(owned, "7e8f9a0b-1c2d-4e3f-8a4b-5c6d7e8f9a0b"),
          on(joined, "8f9a0b1c-2d3e-4f4a-9b5c-6d7e8f9a0b1c"),
        ),
      ),
    );

    // When
    const screen = open(page, { catalogue });

    // Then
    await screen.findByText("Dans Salon");
    expect(trashes(screen)).toHaveLength(1);

    // When
    await fireEvent.click(trash(screen));

    // Then
    await screen.findByText("Retirer un exemplaire de Bibliothèque de Léa ?");
    expect(screen.getByText("Dans Salon")).toBeDefined();
  });

  it.skip("S4 Annuler puts the row back", async () => {
    // Given
    const removal = librisRemoves();
    const screen = open(page, {
      catalogue: librisAnswers(found(heldTwice())),
      copies: removal,
    });
    await asking(screen, "Bibliothèque de Léa");

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Annuler" }));

    // Then
    await screen.findByText("Dans Bibliothèque de Léa");
    expect(screen.getByText("2 exemplaires")).toBeDefined();
    expect(trashes(screen)).toHaveLength(1);
    expect(
      screen.queryByText("Retirer un exemplaire de Bibliothèque de Léa ?"),
    ).toBeNull();
    expect(removal.asked).toEqual([]);
  });

  it.skip("S5 One question at a time", async () => {
    // Given
    const screen = open(page, {
      catalogue: librisAnswers(
        found(
          ouvrage(
            on(owned, "9a0b1c2d-3e4f-4a5b-8c6d-7e8f9a0b1c2d"),
            on(ownedToo, "0b1c2d3e-4f5a-4b6c-9d7e-8f9a0b1c2d3e"),
          ),
        ),
      ),
    });
    await asking(screen, "Bibliothèque de Léa");

    // When
    await fireEvent.click(trash(screen));

    // Then
    await screen.findByText("Retirer un exemplaire de Chambre ?");
    expect(
      screen.queryByText("Retirer un exemplaire de Bibliothèque de Léa ?"),
    ).toBeNull();
    expect(screen.getByText("Dans Bibliothèque de Léa")).toBeDefined();
    expect(trashes(screen)).toHaveLength(1);
  });

  it.skip("S6 Libris unavailable during the removal", async () => {
    // Given
    const screen = open(page, {
      catalogue: librisAnswers(found(heldTwice())),
      copies: librisDoesNotAnswerTheRemoval(),
    });
    await asking(screen, "Bibliothèque de Léa");

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Retirer" }));

    // Then
    await screen.findByText(
      "Erreur lors du retrait, veuillez réessayer plus tard.",
    );
    expect(
      screen.getByText("Retirer un exemplaire de Bibliothèque de Léa ?"),
    ).toBeDefined();
    expect(screen.getByRole("button", { name: "Retirer" })).toBeDefined();
  });

  it.skip("S7 A copy already gone", async () => {
    // Given
    const left = on(owned, "1c2d3e4f-5a6b-4c7d-8e8f-9a0b1c2d3e4f");
    const gone = on(owned, "2d3e4f5a-6b7c-4d8e-9f9a-0b1c2d3e4f5a");
    const screen = open(page, {
      catalogue: librisAnswers(
        found(ouvrage(left, gone)),
        found(ouvrage(left)),
      ),
      copies: librisFindsNoSuchCopy(),
    });
    await asking(screen, "Bibliothèque de Léa");

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Retirer" }));

    // Then
    await waitFor(() => expect(screen.queryByText("2 exemplaires")).toBeNull());
    await screen.findByText("Dans Bibliothèque de Léa");
    expect(
      screen.queryByText(
        "Erreur lors du retrait, veuillez réessayer plus tard.",
      ),
    ).toBeNull();
  });

  it.skip("S8 Removing from the lookup screen, one copy of two", async () => {
    // Given
    const first = on(owned, "3e4f5a6b-7c8d-4e9f-9a0b-1c2d3e4f5a6b");
    const second = on(owned, "4f5a6b7c-8d9e-4f0a-8b1c-2d3e4f5a6b7c");
    const removal = librisRemoves();
    const screen = open("/isbn", {
      isbn: theHouseHolds(onePiece2, [first, second]),
      copies: removal,
    });
    await ask(screen, "9782723489898");
    await screen.findByText("2 exemplaires");

    // When
    await remove(screen);

    // Then
    await waitFor(() => expect(screen.queryByText("2 exemplaires")).toBeNull());
    expect(screen.getByText("Dans Bibliothèque de Léa")).toBeDefined();
    expect(
      screen.getByText("Aux prises avec Baggy et ses hommes"),
    ).toBeDefined();
    expect(window.location.pathname).toBe("/isbn");
    expect(removal.asked).toHaveLength(1);
    expect([first.id, second.id]).toContain(removal.asked[0]);
  });

  it.skip("S8 Removing from the lookup screen, right after the add", async () => {
    // Given
    const added = on(owned, "5a6b7c8d-9e0f-4a1b-9c2d-3e4f5a6b7c8d");
    const removal = librisRemoves();
    const screen = open("/isbn", {
      isbn: aSourceKnows(onePiece1),
      add: new FakeBookshelfApi({ outcome: "added", copy: added }),
      copies: removal,
    });
    await ask(screen, "9782723488525");
    await fireEvent.click(
      await screen.findByRole("button", { name: "Ajouter à ma bibliothèque" }),
    );
    await screen.findByText("Dans Bibliothèque de Léa");

    // When
    await remove(screen);

    // Then
    await screen.findByText("Dans aucune de vos bibliothèques");
    expect(
      screen.getByRole("button", { name: "Ajouter à ma bibliothèque" }),
    ).toBeDefined();
    expect(removal.asked).toEqual([added.id]);
  });

  function on(bookshelf: ReaderBookshelf, copyId: string): Copy {
    return { id: copyId, bookshelf };
  }

  function ouvrage(...copies: Copy[]): Book {
    return { ...asterixLeGaulois, copies };
  }

  function heldTwice(): Book {
    return ouvrage(
      on(owned, "6b7c8d9e-0f1a-4b2c-8d3e-4f5a6b7c8d9e"),
      on(owned, "7c8d9e0f-1a2b-4c3d-9e4f-5a6b7c8d9e0f"),
    );
  }

  function found(book: Book): FindBookAnswer {
    return { outcome: "found", book };
  }

  function notFound(): FindBookAnswer {
    return { outcome: "problem", type: "/problems/not-found" };
  }

  function librisAnswers(...finds: FindBookAnswer[]): CatalogueApi {
    return new FakeCatalogueApi([], finds);
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

  function aSourceKnows(edition: SourceEdition): IsbnApi {
    return new FakeIsbnApi({
      outcome: "found",
      id: null,
      edition,
      copies: [],
      covers: [],
    });
  }

  function librisRemoves() {
    return new FakeCopiesApi({ outcome: "removed" });
  }

  function librisFindsNoSuchCopy() {
    return new FakeCopiesApi({
      outcome: "problem",
      type: "/problems/not-found",
    });
  }

  function librisDoesNotAnswerTheRemoval() {
    return new FakeCopiesApi(new TypeError("Failed to fetch"));
  }

  function open(
    path: string,
    world: {
      catalogue?: CatalogueApi;
      isbn?: IsbnApi;
      add?: BookshelfApi;
      copies?: CopiesApi;
    },
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
      },
      "sha-abc1234",
    );
    app.provide(
      copiesApiKey,
      world.copies ??
        new FakeCopiesApi(new Error("no removal in this scenario")),
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

  async function asking(screen: Screen, bookshelf: string) {
    const [first] = await screen.findAllByRole("button", trashName);
    await fireEvent.click(first);
    await screen.findByText(`Retirer un exemplaire de ${bookshelf} ?`);
  }

  async function remove(screen: Screen) {
    await fireEvent.click(trash(screen));
    await fireEvent.click(
      await screen.findByRole("button", { name: "Retirer" }),
    );
  }

  function trash(screen: Screen) {
    return screen.getByRole("button", trashName);
  }

  function trashes(screen: Screen) {
    return screen.queryAllByRole("button", trashName);
  }
});
