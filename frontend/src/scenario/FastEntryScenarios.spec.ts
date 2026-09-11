import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, inject, it, vi } from "vitest";
import { bootstrap } from "../bootstrap";

type Screen = BoundFunctions<typeof queries>;

describe("Fast entry", () => {
  const host = document.createElement("div");
  let app: ReturnType<typeof bootstrap>;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
    Reflect.deleteProperty(navigator, "mediaDevices");
  });

  it("the application runs over the mock", async () => {
    // When
    const screen = open("/");

    // Then
    expect(await screen.findByText(/^Bonjour /)).toBeDefined();
    expect(screen.getByText("sha-abc1234")).toBeDefined();
  });

  it.skip("S1 Typed ISBN, found, with hyphens", async () => {
    // Given
    const screen = open("/isbn");

    // When
    await ask(screen, "978-2-7234-8852-5");

    // Then
    await showsTheOnePieceCard(screen);
  });

  it.skip("S1 Typed ISBN, found, the old ten", async () => {
    // Given
    const screen = open("/isbn");

    // When
    await ask(screen, "2723488527");

    // Then
    await showsTheOnePieceCard(screen);
  });

  it.skip("S2 Scanned barcode", async () => {
    // Given
    cameraAllowed();
    cameraSees("9782723488525");

    // When
    const screen = open("/isbn");

    // Then
    await showsTheOnePieceCard(screen);
  });

  it.skip("S3 Not an ISBN, wrong length", async () => {
    // Given
    const screen = open("/isbn");
    const requests = vi.spyOn(globalThis, "fetch");

    // When
    await ask(screen, "978272348852");

    // Then
    await screen.findByText(/n'est pas un ISBN valide/);
    expect(host.textContent).toContain(
      "978272348852 n'est pas un ISBN valide.",
    );
    expect(requests).not.toHaveBeenCalled();
  });

  it.skip("S3 Not an ISBN, wrong check digit", async () => {
    // Given
    const screen = open("/isbn");
    const requests = vi.spyOn(globalThis, "fetch");

    // When
    await ask(screen, "9782723488526");

    // Then
    await screen.findByText(/n'est pas un ISBN valide/);
    expect(host.textContent).toContain(
      "9782723488526 n'est pas un ISBN valide.",
    );
    expect(requests).not.toHaveBeenCalled();
  });

  it.skip("S4 Unknown ISBN", async () => {
    // Given
    const screen = open("/isbn");

    // When
    await ask(screen, "9782000000006");

    // Then
    await screen.findByText(/est un ISBN inconnu/);
    expect(host.textContent).toContain("9782000000006 est un ISBN inconnu.");
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

  function open(path: string) {
    window.history.replaceState(null, "", path);
    app = bootstrap(inject("mockBaseUrl"), "sha-abc1234");
    app.mount(host);
    return within(host);
  }

  async function ask(screen: Screen, text: string) {
    const field = screen.getByRole("textbox", { name: "ISBN" });
    await fireEvent.input(field, { target: { value: text } });
    await fireEvent.click(screen.getByRole("button", { name: "Chercher" }));
  }

  async function showsTheOnePieceCard(screen: Screen) {
    await screen.findByText("Romance dawn");
    const card = host.textContent ?? "";
    expect(card).toContain("à l'aube d'une grande aventure");
    expect(card).toContain("Eiichirō Oda");
    expect(card).toContain("scénario");
    expect(card).toContain("dessin");
    expect(card).toMatch(/One piece\D{0,12}1\b/);
    expect(card).toContain("Shonen manga");
    expect(card).toContain("Glénat");
    expect(card).toContain("2013");
    expect(card).toContain("français");
    expect(card).toContain("203");
    expect(card).toContain("9782723488525");
    expect(card).toContain("BnF");
    expect(card).toContain("Open Library");
    const covers = screen
      .getAllByRole("img")
      .map((image) => image.getAttribute("src"));
    expect(covers).toContain(
      "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
    );
  }
});
