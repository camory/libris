import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { flushPromises, mount, type VueWrapper } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { barcodeScannerKey } from "../../../application/BarcodeScanner";
import { isbnApiKey, type IsbnAnswer } from "../../../application/IsbnApi";
import { FakeBarcodeScanner } from "../../../fixture/FakeBarcodeScanner";
import { FakeIsbnApi } from "../../../fixture/FakeIsbnApi";
import { onePiece1 } from "../../../fixture/SourceEditions";
import { createLibrisI18n } from "../../i18n";
import IsbnView from "./IsbnView.vue";

type Screen = BoundFunctions<typeof queries>;

const found: IsbnAnswer = { outcome: "found", edition: onePiece1 };

const unknownIsbn: IsbnAnswer = {
  outcome: "problem",
  type: "/problems/not-found",
};

const sourcesDown: IsbnAnswer = {
  outcome: "problem",
  type: "/problems/sources-unavailable",
};

const neverRead = new Promise<string | null>(() => {});

describe("IsbnView", () => {
  let mounted: VueWrapper;

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
    expect(screen.getByText("BnF")).toBeDefined();
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

  it("stops the camera when the screen goes away", async () => {
    // Given
    const scanner = new FakeBarcodeScanner(true, neverRead);
    open(new FakeIsbnApi(unknownIsbn), scanner);
    await flushPromises();

    // When
    mounted.unmount();

    // Then
    expect(scanner.stops).toBe(1);
  });

  function open(
    api: FakeIsbnApi,
    scanner: FakeBarcodeScanner = new FakeBarcodeScanner(false),
  ) {
    mounted = mount(IsbnView, {
      global: {
        plugins: [createLibrisI18n()],
        provide: { [isbnApiKey]: api, [barcodeScannerKey]: scanner },
      },
    });
    return within(mounted.element as HTMLElement);
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
