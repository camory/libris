import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, inject, it, vi } from "vitest";
import { bootstrap } from "../bootstrap";

type Screen = BoundFunctions<typeof queries>;

describe("Bookshelf", () => {
  const host = document.createElement("div");
  let app: ReturnType<typeof bootstrap>;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
    vi.restoreAllMocks();
  });

  it("S4 The ouvrage is already in a bookshelf", async () => {
    // Given
    const screen = open("/isbn");

    // When
    await ask(screen, "9782723489898");

    // Then
    await screen.findByText("Aux prises avec Baggy et ses hommes");
    expect(rows(screen)).toEqual(["Dans Bibliothèque de Léa", "Dans Salon"]);
    expect(card()).not.toContain("exemplaires");
  });

  it.skip("S4 The ouvrage is already in a bookshelf, none of the reader's", async () => {
    // Given
    const screen = open("/isbn");

    // When
    await ask(screen, "9782723488525");

    // Then
    await screen.findByText("Romance dawn");
    expect(rows(screen)).toEqual(["Dans aucune de vos bibliothèques"]);
  });

  it.skip("S2 The ouvrage is added", async () => {
    // Given
    const screen = open("/isbn");
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");
    expect(rows(screen)).toEqual([]);

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
    const screen = open("/isbn");
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");
    librisDoesNotAnswerTheAdd();

    // When
    await fireEvent.click(addButton(screen));

    // Then
    await screen.findByText(
      "Erreur lors de l'ajout, veuillez réessayer plus tard.",
    );
    expect(rows(screen)).toEqual([]);
    expect(addButton(screen)).toBeDefined();
  });

  function librisDoesNotAnswerTheAdd() {
    const fetch = globalThis.fetch;
    vi.spyOn(globalThis, "fetch").mockImplementation((input, init) =>
      init?.method === "POST"
        ? Promise.reject(new TypeError("Failed to fetch"))
        : fetch(input, init),
    );
  }

  function open(path: string) {
    window.history.replaceState(null, "", path);
    app = bootstrap(inject("mockBaseUrl"), "sha-abc1234");
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
