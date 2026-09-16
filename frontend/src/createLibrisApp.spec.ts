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

describe("createLibrisApp", () => {
  it("renders the home view over the ports it is given", async () => {
    // Given
    const host = document.createElement("div");
    const app = createLibrisApp(
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

    // When
    app.mount(host);
    await flushPromises();

    // Then
    expect(host.textContent).toContain("La bibliothèque de la maison");
    expect(host.textContent).toContain("Bonjour Chloé");
    expect(host.textContent).toContain("sha-abc1234");

    app.unmount();
  });

  it("shows the tab bar under the footer", async () => {
    // Given
    const host = document.createElement("div");
    const app = createLibrisApp(
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

    // When
    app.mount(host);
    await flushPromises();

    // Then
    const shown = host.textContent ?? "";
    expect(shown).toContain("Accueil");
    expect(shown.indexOf("sha-abc1234")).toBeLessThan(shown.indexOf("Accueil"));

    app.unmount();
  });
});
