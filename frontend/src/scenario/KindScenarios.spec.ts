import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, inject, it } from "vitest";
import { bootstrap } from "../bootstrap";

type Screen = BoundFunctions<typeof queries>;

describe("Kind", () => {
  const host = document.createElement("div");
  let app: ReturnType<typeof bootstrap>;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
  });

  it("S1 A manga", async () => {
    // Given
    const screen = open("/isbn");

    // When
    await ask(screen, "9782723488525");

    // Then
    await screen.findByText("Romance dawn");
    const card = host.textContent ?? "";
    expect(card).toMatch(/One piece\D{0,12}tome 1\b/);
    expect(card).toContain("Eiichirō Oda");
    expect(card).not.toContain("scénario");
    expect(card).not.toContain("dessin");
  });

  function open(path: string) {
    window.history.replaceState(null, "", path);
    app = bootstrap(inject("mockBaseUrl"), "sha-abc1234");
    app.mount(host);
    return within(host);
  }

  async function ask(screen: Screen, text: string) {
    const field = screen.getByRole<HTMLInputElement>("textbox", { name: "ISBN" });
    await fireEvent.input(field, { target: { value: text } });
    await fireEvent.click(screen.getByRole("button", { name: "Chercher" }));
  }
});
