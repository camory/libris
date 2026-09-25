import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { barcodeScannerKey } from "../../../application/BarcodeScanner";
import {
  bookshelfApiKey,
  type AddAnswer,
} from "../../../application/BookshelfApi";
import { isbnApiKey, type IsbnAnswer } from "../../../application/IsbnApi";
import { meApiKey } from "../../../application/MeApi";
import { FakeBarcodeScanner } from "../../../fixture/FakeBarcodeScanner";
import { FakeBookshelfApi } from "../../../fixture/FakeBookshelfApi";
import { FakeIsbnApi } from "../../../fixture/FakeIsbnApi";
import { FakeMeApi } from "../../../fixture/FakeMeApi";
import { lea } from "../../../fixture/Readers";
import { onePiece1 } from "../../../fixture/SourceEditions";
import BusySpinner from "../../components/BusySpinner.vue";
import { createLibrisI18n } from "../../i18n";
import IsbnView from "./IsbnView.vue";

type Screen = BoundFunctions<typeof queries>;

const found: IsbnAnswer = {
  outcome: "found",
  edition: onePiece1,
  copies: [],
};

const unknownIsbn: IsbnAnswer = {
  outcome: "problem",
  type: "/problems/not-found",
};

const sourcesDown: IsbnAnswer = {
  outcome: "problem",
  type: "/problems/sources-unavailable",
};

const added: AddAnswer = {
  outcome: "added",
  copy: {
    id: "5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50",
    bookshelf: lea.defaultBookshelf,
  },
};

const neverRead = new Promise<string | null>(() => {});

describe("IsbnView", () => {
  it("renders the title and the hint", () => {
    const screen = open(new FakeIsbnApi(unknownIsbn));

    expect(screen.getByText("Ajouter un ouvrage")).toBeDefined();
    expect(
      screen.getByText("Scannez le code-barres ou saisissez l'ISBN."),
    ).toBeDefined();
  });

  it("refuses a text the rule refuses and asks nothing", async () => {
    // Given
    const api = new FakeIsbnApi(unknownIsbn);
    const screen = open(api);

    // When
    await ask(screen, "978272348852");

    // Then
    expect(screen.getByText("ISBN invalide")).toBeDefined();
    expect(field(screen).value).toBe("978272348852");
    expect(api.asked).toEqual([]);
  });

  it("asks for the ISBN-13 the rule computes and shows the card", async () => {
    // Given
    const api = new FakeIsbnApi(found);
    const screen = open(api);

    // When
    await ask(screen, "978-2-7234-8852-5");

    // Then
    expect(api.asked).toEqual(["9782723488525"]);
    expect(screen.getByText("Romance dawn")).toBeDefined();
    expect(screen.getByText("Glénat")).toBeDefined();
  });

  it("says in which bookshelf the reader already has a copy", async () => {
    // Given
    const shelved: IsbnAnswer = {
      outcome: "found",
      edition: onePiece1,
      copies: [
        {
          id: "6f1d2c3b-4a59-4e6f-8b70-1c2d3e4f5a61",
          bookshelf: {
            id: "0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4e",
            name: "Bibliothèque de Léa",
          },
        },
      ],
    };
    const screen = open(new FakeIsbnApi(shelved));

    // When
    await ask(screen, "9782723488525");

    // Then
    expect(screen.getByText("Dans Bibliothèque de Léa")).toBeDefined();
  });

  it("says when no bookshelf of the reader holds a copy", async () => {
    // Given
    const screen = open(new FakeIsbnApi(found));

    // When
    await ask(screen, "9782723488525");

    // Then
    expect(screen.getByText("Dans aucune de vos bibliothèques")).toBeDefined();
  });

  it("asks for the ISBN-13 an old ten converts to", async () => {
    // Given
    const api = new FakeIsbnApi(found);
    const screen = open(api);

    // When
    await ask(screen, "2723488527");

    // Then
    expect(api.asked).toEqual(["9782723488525"]);
    expect(screen.getByText("Romance dawn")).toBeDefined();
  });

  it("says the ISBN is unknown when no source knows it", async () => {
    // Given
    const screen = open(new FakeIsbnApi(unknownIsbn));

    // When
    await ask(screen, "9782000000006");

    // Then
    expect(screen.getByText("ISBN inconnu")).toBeDefined();
  });

  it("asks to try again later when no source answered", async () => {
    // Given
    const screen = open(new FakeIsbnApi(sourcesDown));

    // When
    await ask(screen, "9791000000008");

    // Then
    expect(
      screen.getByText(
        "Erreur lors de la recherche, veuillez réessayer plus tard.",
      ),
    ).toBeDefined();
  });

  it("falls back to the same sentence for a problem it does not know", async () => {
    // Given
    const teapot: IsbnAnswer = { outcome: "problem", type: "/problems/teapot" };
    const screen = open(new FakeIsbnApi(teapot));

    // When
    await ask(screen, "9782723488525");

    // Then
    expect(
      screen.getByText(
        "Erreur lors de la recherche, veuillez réessayer plus tard.",
      ),
    ).toBeDefined();
  });

  it("falls back to the same sentence for a problem named like an object member", async () => {
    // Given
    const member: IsbnAnswer = { outcome: "problem", type: "constructor" };
    const screen = open(new FakeIsbnApi(member));

    // When
    await ask(screen, "9782723488525");

    // Then
    expect(
      screen.getByText(
        "Erreur lors de la recherche, veuillez réessayer plus tard.",
      ),
    ).toBeDefined();
  });

  it("replaces the answer it showed by the next one", async () => {
    // Given
    const screen = open(new FakeIsbnApi(found));
    await ask(screen, "978-2-7234-8852-5");

    // When
    await ask(screen, "978272348852");

    // Then
    expect(screen.getByText("ISBN invalide")).toBeDefined();
    expect(screen.queryByText("Romance dawn")).toBeNull();
  });

  it("is busy while the lookup runs, over a field that stays editable", async () => {
    // Given
    const screen = open(new FakeIsbnApi(new Promise<IsbnAnswer>(() => {})));

    // When
    await ask(screen, "9782723488525");

    // Then
    const button = screen.getByRole<HTMLButtonElement>("button", {
      name: "Recherche en cours…",
    });
    expect(button.disabled).toBe(true);
    expect(field(screen).disabled).toBe(false);
  });

  it("comes back to itself once the answer lands", async () => {
    // Given
    const screen = open(new FakeIsbnApi(sourcesDown));

    // When
    await ask(screen, "9791000000008");

    // Then
    expect(
      screen.getByRole<HTMLButtonElement>("button", { name: "Chercher" })
        .disabled,
    ).toBe(false);
  });

  it("offers the field alone where the browser detects no barcode", async () => {
    // Given
    const scanner = new FakeBarcodeScanner(false);

    // When
    const screen = open(new FakeIsbnApi(unknownIsbn), scanner);
    await flushPromises();

    // Then
    expect(
      screen.queryByRole("button", { name: "Scanner le code-barres" }),
    ).toBeNull();
    expect(
      screen.queryByRole("button", { name: "Fermer la caméra" }),
    ).toBeNull();
    expect(scanner.readsInto).toEqual([]);
  });

  it("opens the camera by itself where the browser detects barcodes", async () => {
    // Given
    const scanner = new FakeBarcodeScanner(true, neverRead);

    // When
    const screen = open(new FakeIsbnApi(unknownIsbn), scanner);
    await flushPromises();

    // Then
    expect(
      screen.getByRole("button", { name: "Fermer la caméra" }),
    ).toBeDefined();
    expect(scanner.readsInto).toHaveLength(1);
  });

  it("runs the lookup with the first code the camera reads", async () => {
    // Given
    const api = new FakeIsbnApi(found);
    const scanner = new FakeBarcodeScanner(true, "9782723488525");

    // When
    const screen = open(api, scanner);
    await flushPromises();

    // Then
    expect(api.asked).toEqual(["9782723488525"]);
    expect(field(screen).value).toBe("9782723488525");
    expect(screen.getByText("Romance dawn")).toBeDefined();
    expect(
      screen.getByRole("button", { name: "Scanner le code-barres" }),
    ).toBeDefined();
  });

  it("closes the camera on the cross and opens it again on the barcode", async () => {
    // Given
    const scanner = new FakeBarcodeScanner(true, neverRead);
    const screen = open(new FakeIsbnApi(unknownIsbn), scanner);
    await flushPromises();

    // When
    await press(screen, "Fermer la caméra");
    await press(screen, "Scanner le code-barres");

    // Then
    expect(scanner.stops).toBe(1);
    expect(scanner.readsInto).toHaveLength(2);
  });

  it("says nothing when the reader refuses the camera", async () => {
    // Given
    const scanner = new FakeBarcodeScanner(true, null);

    // When
    const screen = open(new FakeIsbnApi(unknownIsbn), scanner);
    await flushPromises();

    // Then
    expect(
      screen.queryByRole("button", { name: "Fermer la caméra" }),
    ).toBeNull();
    expect(screen.queryByText("ISBN invalide")).toBeNull();
    expect(screen.queryByText("ISBN inconnu")).toBeNull();
    expect(
      screen.queryByText(
        "Erreur lors de la recherche, veuillez réessayer plus tard.",
      ),
    ).toBeNull();
    expect(
      screen.getByRole("button", { name: "Scanner le code-barres" }),
    ).toBeDefined();
  });

  it("offers to add the ouvrage under the card", async () => {
    // Given
    const screen = open(new FakeIsbnApi(found));

    // When
    await ask(screen, "9782723488525");

    // Then
    const button = screen.getByRole("button", {
      name: "Ajouter à ma bibliothèque",
    });
    expect(
      screen.getByText("9782723488525").compareDocumentPosition(button) &
        Node.DOCUMENT_POSITION_FOLLOWING,
    ).toBeTruthy();
  });

  it("offers to add another copy of an ouvrage the reader has", async () => {
    // Given
    const shelved: IsbnAnswer = {
      outcome: "found",
      edition: onePiece1,
      copies: [
        {
          id: "6f1d2c3b-4a59-4e6f-8b70-1c2d3e4f5a61",
          bookshelf: lea.defaultBookshelf,
        },
      ],
    };
    const screen = open(new FakeIsbnApi(shelved));

    // When
    await ask(screen, "9782723488525");

    // Then
    expect(
      screen.getByRole("button", { name: "Ajouter à ma bibliothèque" }),
    ).toBeDefined();
  });

  it("offers no add without a card", async () => {
    // Given
    const screen = open(new FakeIsbnApi(unknownIsbn));

    // When
    await ask(screen, "9782000000006");

    // Then
    expect(
      screen.queryByRole("button", { name: "Ajouter à ma bibliothèque" }),
    ).toBeNull();
  });

  it("is busy while the add runs", async () => {
    // Given
    const view = mountView(
      new FakeIsbnApi(found),
      undefined,
      new FakeBookshelfApi(new Promise<AddAnswer>(() => {})),
    );
    const screen = within(view.element as HTMLElement);
    await ask(screen, "9782723488525");

    // When
    await press(screen, "Ajouter à ma bibliothèque");

    // Then
    const button = screen.getByRole<HTMLButtonElement>("button", {
      name: "Ajout en cours…",
    });
    expect(button.disabled).toBe(true);
    expect(button.contains(view.findComponent(BusySpinner).element)).toBe(
      true,
    );
    expect(
      screen.queryByRole("button", { name: "Ajouter à ma bibliothèque" }),
    ).toBeNull();
  });

  it("adds the ouvrage of the card to the reader's default bookshelf", async () => {
    // Given
    const bookshelfApi = new FakeBookshelfApi(added);
    const screen = open(new FakeIsbnApi(found), undefined, bookshelfApi);
    await ask(screen, "9782723488525");

    // When
    await press(screen, "Ajouter à ma bibliothèque");

    // Then
    expect(bookshelfApi.asked).toEqual([
      { bookshelfId: lea.defaultBookshelf.id, edition: onePiece1 },
    ]);
  });

  it("shows the copy it added and offers no more to add", async () => {
    // Given
    const screen = open(
      new FakeIsbnApi(found),
      undefined,
      new FakeBookshelfApi(added),
    );
    await ask(screen, "9782723488525");

    // When
    await press(screen, "Ajouter à ma bibliothèque");

    // Then
    expect(screen.getByText("Dans Bibliothèque de Léa")).toBeDefined();
    expect(screen.queryByText("Dans aucune de vos bibliothèques")).toBeNull();
    expect(
      screen.queryByRole("button", { name: "Ajouter à ma bibliothèque" }),
    ).toBeNull();
    expect(screen.queryByRole("button", { name: "Ajout en cours…" })).toBeNull();
  });

  it("stops the camera when the screen goes away", async () => {
    // Given
    const scanner = new FakeBarcodeScanner(true, neverRead);
    const view = mountView(new FakeIsbnApi(unknownIsbn), scanner);
    await flushPromises();

    // When
    view.unmount();

    // Then
    expect(scanner.stops).toBe(1);
  });

  function open(
    api: FakeIsbnApi,
    scanner?: FakeBarcodeScanner,
    bookshelfApi?: FakeBookshelfApi,
  ) {
    return within(
      mountView(api, scanner, bookshelfApi).element as HTMLElement,
    );
  }

  function mountView(
    api: FakeIsbnApi,
    scanner: FakeBarcodeScanner = new FakeBarcodeScanner(false),
    bookshelfApi = new FakeBookshelfApi(new Error("no add in this case")),
  ) {
    return mount(IsbnView, {
      global: {
        plugins: [createLibrisI18n()],
        provide: {
          [isbnApiKey]: api,
          [barcodeScannerKey]: scanner,
          [meApiKey]: new FakeMeApi(lea),
          [bookshelfApiKey]: bookshelfApi,
        },
      },
    });
  }

  async function ask(screen: Screen, text: string) {
    await fireEvent.input(field(screen), { target: { value: text } });
    await press(screen, "Chercher");
  }

  async function press(screen: Screen, name: string) {
    await fireEvent.click(screen.getByRole("button", { name }));
    await flushPromises();
  }

  function field(screen: Screen) {
    return screen.getByRole<HTMLInputElement>("textbox", { name: "ISBN" });
  }
});
