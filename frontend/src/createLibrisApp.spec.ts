import { within } from "@testing-library/dom";
import { flushPromises } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createLibrisApp } from "./createLibrisApp";
import { FakeAppUpdate } from "./fixture/FakeAppUpdate";
import { FakeBookshelfApi } from "./fixture/FakeBookshelfApi";
import { FakeBarcodeScanner } from "./fixture/FakeBarcodeScanner";
import { FakeIsbnApi } from "./fixture/FakeIsbnApi";
import { FakeMeApi } from "./fixture/FakeMeApi";
import { chloe } from "./fixture/Readers";

const librisApp = () =>
  createLibrisApp(
    {
      meApi: new FakeMeApi(chloe),
      isbnApi: new FakeIsbnApi({
        outcome: "problem",
        type: "/problems/not-found",
      }),
      bookshelfApi: new FakeBookshelfApi({
        outcome: "problem",
        type: "/problems/not-found",
      }),
      barcodeScanner: new FakeBarcodeScanner(false),
      appUpdate: new FakeAppUpdate(),
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
