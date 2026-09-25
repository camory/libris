import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, it } from "vitest";
import type { App } from "vue";
import type { IsbnApi } from "../application/IsbnApi";
import { createLibrisApp } from "../createLibrisApp";
import type { SourceEdition } from "../domain/SourceEdition";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import { FakeBarcodeScanner } from "../fixture/FakeBarcodeScanner";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeIsbnApi } from "../fixture/FakeIsbnApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { lea } from "../fixture/Readers";
import { onePiece1 } from "../fixture/SourceEditions";

type Screen = BoundFunctions<typeof queries>;

describe("Kind", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
  });

  it("S1 A manga", async () => {
    // Given
    const screen = open("/isbn", { isbn: aSourceKnows(onePiece1) });

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

  function aSourceKnows(edition: SourceEdition): IsbnApi {
    return new FakeIsbnApi({ outcome: "found", edition, copies: [] });
  }

  function open(path: string, world: { isbn: IsbnApi }) {
    window.history.replaceState(null, "", path);
    app = createLibrisApp(
      {
        meApi: new FakeMeApi(lea),
        isbnApi: world.isbn,
        bookshelfApi: new FakeBookshelfApi(
          new Error("no add in this scenario"),
        ),
        barcodeScanner: new FakeBarcodeScanner(false),
        appUpdate: new FakeAppUpdate(),
      },
      "sha-abc1234",
    );
    app.mount(host);
    return within(host);
  }

  async function ask(screen: Screen, text: string) {
    const field = screen.getByRole<HTMLInputElement>("textbox", {
      name: "ISBN",
    });
    await fireEvent.input(field, { target: { value: text } });
    await fireEvent.click(screen.getByRole("button", { name: "Chercher" }));
  }
});
