import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { nextTick } from "vue";
import type { SourceEdition } from "../../domain/SourceEdition";
import { onePiece1 } from "../../fixture/SourceEditions";
import { createLibrisI18n } from "../i18n";
import IconBook from "./icons/IconBook.vue";
import SourceEditionCard from "./SourceEditionCard.vue";

const barelyKnown: SourceEdition = {
  ...onePiece1,
  subtitle: null,
  authors: [],
  series: null,
  collection: null,
  publisher: null,
  publicationYear: null,
  language: null,
  pageCount: null,
  summary: null,
  coverUrl: null,
};

const asterix1: SourceEdition = {
  ...onePiece1,
  kind: "BD",
  title: "Astérix le Gaulois",
  subtitle: null,
  authors: [
    { name: "René Goscinny", role: "WRITER" },
    { name: "Albert Uderzo", role: "ARTIST" },
  ],
  series: { name: "Astérix", volumeNumber: 1 },
};

describe("SourceEditionCard", () => {
  it("shows the series and the volume, the title and the subtitle", () => {
    // When
    const card = show(onePiece1);

    // Then
    expect(card).toContain("One piece · tome 1");
    expect(card).toContain("Romance dawn");
    expect(card).toContain("à l'aube d'une grande aventure");
  });

  it("shows the volume of a BD as an album", () => {
    // When
    const card = show(asterix1);

    // Then
    expect(card).toContain("Astérix · album 1");
    expect(card).not.toContain("tome");
  });

  it("shows one line per author, with the French words of its roles", () => {
    // When
    const card = show(onePiece1);

    // Then
    expect(card).toContain("Eiichirō Oda · scénario, dessin");
    expect(card.match(/Eiichirō Oda/g)).toHaveLength(1);
  });

  it("shows one row per field, in the order of the card", () => {
    // When
    const card = show(onePiece1);

    // Then
    const parts = [
      "Collection",
      "Shonen manga",
      "Éditeur",
      "Glénat",
      "Année",
      "2013",
      "Langue",
      "français",
      "Pages",
      "203",
      "ISBN",
      "9782723488525",
    ];
    const positions = parts.map((part) => card.indexOf(part));
    expect(positions).not.toContain(-1);
    expect(positions).toEqual([...positions].sort((a, b) => a - b));
  });

  it("gives no row to a field the sources did not give", () => {
    // Given
    const partly: SourceEdition = {
      ...onePiece1,
      collection: null,
      publisher: null,
      publicationYear: null,
      language: null,
      pageCount: null,
    };

    // When
    const card = show(partly);

    // Then
    for (const label of ["Collection", "Éditeur", "Année", "Langue", "Pages"]) {
      expect(card).not.toContain(label);
    }
    expect(card).toContain("ISBN");
    expect(card).toContain("9782723488525");
    expect(card).not.toContain("inconnu");
  });

  it("shows the title and the ISBN of an edition the sources barely know", () => {
    // When
    const card = show(barelyKnown);

    // Then
    expect(card).toContain("Romance dawn");
    expect(card).toContain("9782723488525");
    expect(card).not.toContain("tome");
    expect(card).not.toContain("·");
    expect(card).not.toContain("inconnu");
  });

  it("shows the summary of the sources, and nothing when they gave none", () => {
    // Given
    const summary = "Luffy prend la mer pour devenir le roi des pirates.";

    // When
    const card = show({ ...onePiece1, summary });

    // Then
    expect(card).toContain(summary);
    expect(show(onePiece1)).not.toContain(summary);
  });

  it("shows the cover the sources gave, named after the ouvrage", () => {
    // When
    const covers = screen(onePiece1).getAllByRole("img");

    // Then
    expect(covers).toHaveLength(1);
    expect(covers[0].getAttribute("src")).toBe(
      "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
    );
    expect(covers[0].getAttribute("alt")).toContain("Romance dawn");
    expect(
      screen({ ...onePiece1, coverUrl: null }).queryAllByRole("img"),
    ).toEqual([]);
    expect(card(onePiece1).findComponent(IconBook).exists()).toBe(false);
  });

  it("shows a book icon when the sources gave no cover", () => {
    // When
    const wrapper = card({ ...onePiece1, coverUrl: null });

    // Then
    expect(
      within(wrapper.element as HTMLElement).queryAllByRole("img"),
    ).toEqual([]);
    expect(wrapper.findComponent(IconBook).exists()).toBe(true);
  });

  it("shows a book icon when the cover does not load", async () => {
    // Given
    const wrapper = card(onePiece1);
    const shown = within(wrapper.element as HTMLElement);

    // When
    shown.getByRole("img").dispatchEvent(new Event("error"));
    await nextTick();

    // Then
    expect(shown.queryAllByRole("img")).toEqual([]);
    expect(wrapper.findComponent(IconBook).exists()).toBe(true);
  });

  it("never says which source answered", () => {
    // When
    const card = show(onePiece1);

    // Then
    expect(card).not.toContain("Sources");
    expect(card).not.toContain("BnF");
    expect(card).not.toContain("Open Library");
  });

  it("shows the série alone when the sources gave it no tome", () => {
    // Given
    const standalone: SourceEdition = {
      ...onePiece1,
      series: { name: "One piece", volumeNumber: null },
    };

    // When
    const card = show(standalone);

    // Then
    expect(card).toContain("One piece");
    expect(card).not.toContain("tome");
  });

  it("shows the code of a language the catalogue has no word for", () => {
    // When
    const card = screen({ ...onePiece1, language: "en" });

    // Then
    expect(card.getByText("Langue")).toBeDefined();
    expect(card.getByText("en")).toBeDefined();
    expect(card.queryByText("language.en")).toBeNull();
  });

  function show(edition: SourceEdition) {
    return card(edition).text().replace(/\s+/g, " ");
  }

  function screen(edition: SourceEdition) {
    return within(card(edition).element as HTMLElement);
  }

  function card(edition: SourceEdition) {
    return mount(SourceEditionCard, {
      props: { edition },
      global: { plugins: [createLibrisI18n()] },
    });
  }
});
