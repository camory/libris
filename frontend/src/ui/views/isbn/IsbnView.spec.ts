import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { isbnApiKey, type IsbnAnswer } from "../../../application/IsbnApi";
import type { SourceEdition } from "../../../domain/SourceEdition";
import { FakeIsbnApi } from "../../../fixture/FakeIsbnApi";
import { createLibrisI18n } from "../../i18n";
import IsbnView from "./IsbnView.vue";

type Screen = BoundFunctions<typeof queries>;

const onePiece: SourceEdition = {
  isbn13: "9782723488525",
  title: "Romance dawn",
  subtitle: "à l'aube d'une grande aventure",
  authors: [{ name: "Eiichirō Oda", role: "WRITER" }],
  series: { name: "One piece", volumeNumber: 1 },
  collection: "Shonen manga",
  publisher: "Glénat",
  publicationYear: 2013,
  language: "fr",
  pageCount: 203,
  summary: null,
  coverUrl: "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
  sources: ["BNF", "OPEN_LIBRARY"],
};

const found: IsbnAnswer = { outcome: "found", edition: onePiece };

const unknownIsbn: IsbnAnswer = {
  outcome: "problem",
  type: "/problems/not-found",
};

const sourcesDown: IsbnAnswer = {
  outcome: "problem",
  type: "/problems/sources-unavailable",
};

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

  it("asks for the ISBN-13 the rule computes and shows the title", async () => {
    // Given
    const api = new FakeIsbnApi(found);
    const screen = open(api);

    // When
    await ask(screen, "978-2-7234-8852-5");

    // Then
    expect(api.asked).toEqual(["9782723488525"]);
    expect(screen.getByText("Romance dawn")).toBeDefined();
    expect(screen.queryByText("Glénat")).toBeNull();
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

  function open(api: FakeIsbnApi) {
    const wrapper = mount(IsbnView, {
      global: {
        plugins: [createLibrisI18n()],
        provide: { [isbnApiKey]: api },
      },
    });
    return within(wrapper.element as HTMLElement);
  }

  async function ask(screen: Screen, text: string) {
    await fireEvent.input(field(screen), { target: { value: text } });
    await fireEvent.click(screen.getByRole("button", { name: "Chercher" }));
    await flushPromises();
  }

  function field(screen: Screen) {
    return screen.getByRole<HTMLInputElement>("textbox", { name: "ISBN" });
  }
});
