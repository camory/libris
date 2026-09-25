import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, inject, it, vi } from "vitest";
import type { App } from "vue";
import type { BarcodeScanner } from "../application/BarcodeScanner";
import type { IsbnApi } from "../application/IsbnApi";
import { bootstrap } from "../bootstrap";
import { createLibrisApp } from "../createLibrisApp";
import type { SourceEdition } from "../domain/SourceEdition";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import { FakeBarcodeScanner } from "../fixture/FakeBarcodeScanner";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeIsbnApi } from "../fixture/FakeIsbnApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { lea } from "../fixture/Readers";
import { onePiece1 } from "../fixture/SourceEditions";
import { CameraBarcodeScanner } from "../infra/camera/CameraBarcodeScanner";

type Screen = BoundFunctions<typeof queries>;

describe("Fast entry", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
    Reflect.deleteProperty(navigator, "mediaDevices");
  });

  it("the application runs over the mock", async () => {
    // When
    const screen = openOverTheMock("/");

    // Then
    expect(await screen.findByText(/^Bonjour /)).toBeDefined();
    expect(screen.getByText("sha-abc1234")).toBeDefined();
  });

  it("S1 Typed ISBN, found, with hyphens", async () => {
    // Given
    const screen = open("/isbn", { isbn: aSourceKnows(onePiece1) });

    // When
    await ask(screen, "978-2-7234-8852-5");

    // Then
    await showsTheOnePieceCard(screen);
  });

  it("S1 Typed ISBN, found, the old ten", async () => {
    // Given
    const screen = open("/isbn", { isbn: aSourceKnows(onePiece1) });

    // When
    await ask(screen, "2723488527");

    // Then
    await showsTheOnePieceCard(screen);
  });

  it("S2 Scanned barcode", async () => {
    // Given
    cameraAllowed();
    cameraSees("9782723488525");

    // When
    const screen = open("/isbn", {
      isbn: aSourceKnows(onePiece1),
      scanner: new CameraBarcodeScanner(),
    });

    // Then
    await showsTheOnePieceCard(screen);
  });

  it("S3 Not an ISBN, wrong length", async () => {
    // Given
    const screen = open("/isbn", { isbn: aSourceKnows(onePiece1) });

    // When
    await ask(screen, "978272348852");

    // Then
    await screen.findByText("ISBN invalide");
    expect(field(screen).value).toBe("978272348852");
    expect(card()).not.toContain("Romance dawn");
  });

  it("S3 Not an ISBN, wrong check digit", async () => {
    // Given
    const screen = open("/isbn", { isbn: aSourceKnows(onePiece1) });

    // When
    await ask(screen, "9782723488526");

    // Then
    await screen.findByText("ISBN invalide");
    expect(field(screen).value).toBe("9782723488526");
    expect(card()).not.toContain("Romance dawn");
  });

  it("S4 Unknown ISBN", async () => {
    // Given
    const screen = open("/isbn", { isbn: noSourceKnows() });

    // When
    await ask(screen, "9782000000006");

    // Then
    await screen.findByText("ISBN inconnu");
    expect(field(screen).value).toBe("9782000000006");
  });

  it("S7 Every source down", async () => {
    // Given
    const screen = open("/isbn", { isbn: theSourcesAreDown() });

    // When
    await ask(screen, "9791000000008");

    // Then
    await screen.findByText(
      "Erreur lors de la recherche, veuillez réessayer plus tard.",
    );
  });

  function cameraAllowed() {
    const stream = { getTracks: () => [{ stop: () => {} }] };
    Object.defineProperty(navigator, "mediaDevices", {
      configurable: true,
      value: { getUserMedia: () => Promise.resolve(stream) },
    });
    vi.spyOn(HTMLMediaElement.prototype, "play").mockResolvedValue();
  }

  function cameraSees(ean13: string) {
    vi.stubGlobal(
      "BarcodeDetector",
      class {
        static getSupportedFormats() {
          return Promise.resolve(["ean_13"]);
        }
        detect() {
          return Promise.resolve([{ rawValue: ean13, format: "ean_13" }]);
        }
      },
    );
  }

  function aSourceKnows(edition: SourceEdition): IsbnApi {
    return new FakeIsbnApi({ outcome: "found", edition, copies: [] });
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

  function open(
    path: string,
    world: { isbn: IsbnApi; scanner?: BarcodeScanner },
  ) {
    window.history.replaceState(null, "", path);
    app = createLibrisApp(
      {
        meApi: new FakeMeApi(lea),
        isbnApi: world.isbn,
        bookshelfApi: new FakeBookshelfApi(
          new Error("no add in this scenario"),
        ),
        barcodeScanner: world.scanner ?? new FakeBarcodeScanner(false),
        appUpdate: new FakeAppUpdate(),
      },
      "sha-abc1234",
    );
    app.mount(host);
    return within(host);
  }

  function openOverTheMock(path: string) {
    window.history.replaceState(null, "", path);
    app = bootstrap(inject("mockBaseUrl"), "sha-abc1234");
    app.mount(host);
    return within(host);
  }

  function card() {
    return host.textContent ?? "";
  }

  async function ask(screen: Screen, text: string) {
    await fireEvent.input(field(screen), { target: { value: text } });
    await fireEvent.click(screen.getByRole("button", { name: "Chercher" }));
  }

  function field(screen: Screen) {
    return screen.getByRole<HTMLInputElement>("textbox", { name: "ISBN" });
  }

  async function showsTheOnePieceCard(screen: Screen) {
    await screen.findByText("Romance dawn");
    expect(card()).toContain("à l'aube d'une grande aventure");
    expect(card()).toContain("Eiichirō Oda");
    expect(card()).toMatch(/One piece\D{0,12}1\b/);
    expect(card()).toContain("Shonen manga");
    expect(card()).toContain("Glénat");
    expect(card()).toContain("2013");
    expect(card()).toContain("français");
    expect(card()).toContain("203");
    expect(card()).toContain("9782723488525");
    const covers = screen
      .getAllByRole("img")
      .map((image) => image.getAttribute("src"));
    expect(covers).toContain(
      "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
    );
  }
});
