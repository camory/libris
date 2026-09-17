import { within } from "@testing-library/dom";
import { flushPromises } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createLibrisApp } from "./createLibrisApp";
import type { Reader } from "./domain/Reader";
import { FakeBarcodeScanner } from "./fixture/FakeBarcodeScanner";
import { FakeIsbnApi } from "./fixture/FakeIsbnApi";
import { FakeMeApi } from "./fixture/FakeMeApi";

const chloe: Reader = {
  id: "0199c0de-1000-7000-8000-000000000001",
  username: "chloe",
  displayName: "Chloé",
  email: "chloe@amory.fr",
  role: "READER",
};

const librisApp = () =>
  createLibrisApp(
    {
      meApi: new FakeMeApi(chloe),
      isbnApi: new FakeIsbnApi({
        outcome: "problem",
        type: "/problems/not-found",
      }),
      barcodeScanner: new FakeBarcodeScanner(false),
    },
    "sha-abc1234",
  );

describe("createLibrisApp", () => {
  it("renders the home view over the ports it is given", async () => {
    // Given
    const host = document.createElement("div");
    const app = librisApp();

    // When
    app.mount(host);
    await flushPromises();

    // Then
    expect(host.textContent).toContain("La bibliothèque de la maison");
    expect(host.textContent).toContain("Bonjour Chloé");
    expect(host.textContent).toContain("sha-abc1234");

    app.unmount();
  });

  it("shows the tab bar under the screen", async () => {
    // Given
    const host = document.createElement("div");
    const app = librisApp();

    // When
    app.mount(host);
    await flushPromises();

    // Then
    const screen = within(host);
    const revision = screen.getByText("sha-abc1234");
    const home = screen.getByRole("link", { name: "Accueil" });
    screen.getByRole("link", { name: "Ajouter" });
    expect(revision.compareDocumentPosition(home)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING,
    );

    app.unmount();
  });
});
