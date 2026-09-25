import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { nextTick } from "vue";
import type { Copy } from "../../domain/Copy";
import type { SourceAuthor, SourceEdition } from "../../domain/SourceEdition";
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

const lAmiFritz: SourceEdition = {
  ...onePiece1,
  kind: "BOOK",
  title: "L'ami Fritz",
  subtitle: null,
  authors: [
    { name: "Erckmann", role: "WRITER" },
    { name: "Chatrian", role: "WRITER" },
  ],
  series: { name: "Contes et romans", volumeNumber: 1 },
};

const onLea: Copy = {
  id: "6f1d2c3b-4a59-4e6f-8b70-1c2d3e4f5a61",
  bookshelf: {
    id: "0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4e",
    name: "Bibliothèque de Léa",
  },
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

  it("shows the volume of a livre as a tome", () => {
    // When
    const card = show(lAmiFritz);

    // Then
    expect(card).toContain("Contes et romans · tome 1");
    expect(card).not.toContain("album");
  });

  it("names an author alone when they share their roles with everyone", () => {
    // When
    const card = show(onePiece1);

    // Then
    expect(card).toContain("Eiichirō Oda");
    expect(card.match(/Eiichirō Oda/g)).toHaveLength(1);
    expect(card).not.toContain("scénario");
    expect(card).not.toContain("dessin");
  });

  it("names the authors of one set on one line, separated by commas", () => {
    // When
    const card = show(lAmiFritz);

    // Then
    expect(card).toContain("Erckmann, Chatrian");
    expect(card).not.toContain("texte");
  });

  it("says scénario and dessin under a manga", () => {
    // Given
    const drawnByAnother: SourceEdition = {
      ...onePiece1,
      authors: [
        { name: "Eiichirō Oda", role: "WRITER" },
        { name: "Boichi", role: "ARTIST" },
      ],
    };

    // When
    const card = show(drawnByAnother);

    // Then
    expect(card).toContain("Eiichirō Oda · scénario");
    expect(card).toContain("Boichi · dessin");
  });

  it("says scénario and dessin under a BD", () => {
    // When
    const card = show(asterix1);

    // Then
    expect(card).toContain("René Goscinny · scénario");
    expect(card).toContain("Albert Uderzo · dessin");
  });

  it("says texte and illustration under a livre", () => {
    // Given
    const illustrated: SourceEdition = {
      ...lAmiFritz,
      authors: [
        { name: "Erckmann", role: "WRITER" },
        { name: "Théophile Schuler", role: "ARTIST" },
      ],
    };

    // When
    const card = show(illustrated);

    // Then
    expect(card).toContain("Erckmann · texte");
    expect(card).toContain("Théophile Schuler · illustration");
    expect(card).not.toContain("scénario");
    expect(card).not.toContain("dessin");
  });

  it("reads the same roles in another order as the same set", () => {
    // Given
    const inAnyOrder: SourceEdition = {
      ...onePiece1,
      authors: [
        { name: "Eiichirō Oda", role: "WRITER" },
        { name: "Eiichirō Oda", role: "ARTIST" },
        { name: "Boichi", role: "ARTIST" },
        { name: "Boichi", role: "WRITER" },
      ],
    };

    // When
    const card = show(inAnyOrder);

    // Then
    expect(card).toContain("Eiichirō Oda, Boichi");
    expect(card).not.toContain("scénario");
  });

  it("names an author listed twice under one role once, with the role once", () => {
    // Given
    const listedTwice: SourceEdition = {
      ...onePiece1,
      authors: [
        { name: "Eiichirō Oda", role: "WRITER" },
        { name: "Eiichirō Oda", role: "WRITER" },
        { name: "Boichi", role: "WRITER" },
      ],
    };

    // When
    const card = show(listedTwice);

    // Then
    expect(card).toContain("Eiichirō Oda, Boichi");
    expect(card).not.toContain("scénario");
  });

  it("tells apart the authors whose sets of roles differ", () => {
    // Given
    const drawnTogether: SourceEdition = {
      ...onePiece1,
      authors: [
        { name: "Eiichirō Oda", role: "WRITER" },
        { name: "Eiichirō Oda", role: "ARTIST" },
        { name: "Boichi", role: "WRITER" },
      ],
    };

    // When
    const card = show(drawnTogether);

    // Then
    expect(card).toContain("Eiichirō Oda · scénario, dessin");
    expect(card).toContain("Boichi · scénario");
  });

  it("shows no author line when the sources named no author", () => {
    // When
    const card = show({ ...onePiece1, authors: [] });

    // Then
    expect(card).not.toContain("Eiichirō Oda");
    expect(card).toContain(
      "à l'aube d'une grande aventureDans aucune de vos bibliothèques",
    );
  });

  it("says couleurs and traduction under every kind", () => {
    // Given
    const authors: SourceAuthor[] = [
      { name: "Jérémy Petiqueux", role: "COLOURIST" },
      { name: "Sylvain Chollet", role: "TRANSLATOR" },
    ];

    // When
    const cards = [onePiece1, asterix1, lAmiFritz].map((edition) =>
      show({ ...edition, authors }),
    );

    // Then
    for (const card of cards) {
      expect(card).toContain("Jérémy Petiqueux · couleurs");
      expect(card).toContain("Sylvain Chollet · traduction");
    }
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
    expect(card(onePiece1).findAllComponents(IconBook)).toHaveLength(1);
  });

  it("shows a book icon when the sources gave no cover", () => {
    // When
    const wrapper = card({ ...onePiece1, coverUrl: null });

    // Then
    expect(
      within(wrapper.element as HTMLElement).queryAllByRole("img"),
    ).toEqual([]);
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(2);
  });

  it("shows a book icon when the cover does not load", async () => {
    // Given
    const wrapper = card(onePiece1);
    const shown = within(wrapper.element as HTMLElement);
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(1);

    // When
    shown.getByRole("img").dispatchEvent(new Event("error"));
    await nextTick();

    // Then
    expect(shown.queryAllByRole("img")).toEqual([]);
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(2);
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

  it("says in which bookshelf the copy is, between the authors and the rows", () => {
    // When
    const card = show(onePiece1, [onLea]);

    // Then
    const positions = [
      "Eiichirō Oda",
      "Dans Bibliothèque de Léa",
      "Collection",
    ].map((part) => card.indexOf(part));
    expect(positions).not.toContain(-1);
    expect(positions).toEqual([...positions].sort((a, b) => a - b));
  });

  it("counts the copies of a bookshelf that holds more than one, on its row", () => {
    // Given
    const secondOnLea: Copy = {
      ...onLea,
      id: "7a2e3d4c-5b6a-4f70-9c81-2d3e4f5a6b72",
    };

    // When
    const card = show(onePiece1, [onLea, secondOnLea]);

    // Then
    expect(
      card.match(/Dans Bibliothèque de Léa · 2 exemplaires/g),
    ).toHaveLength(1);
    expect(card.match(/Dans/g)).toHaveLength(1);
  });

  it("gives each bookshelf its row, in the order of the answer, with no count", () => {
    // Given
    const onSalon: Copy = {
      id: "7a2e3d4c-5b6a-4f70-9c81-2d3e4f5a6b72",
      bookshelf: { id: "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f", name: "Salon" },
    };

    // When
    const card = show(onePiece1, [onSalon, onLea]);

    // Then
    const salon = card.indexOf("Dans Salon");
    const lea = card.indexOf("Dans Bibliothèque de Léa");
    expect(salon).not.toBe(-1);
    expect(lea).toBeGreaterThan(salon);
    expect(card).not.toContain("exemplaires");
  });

  it("shows the book icon before the bookshelf's words", () => {
    // When
    const wrapper = card(onePiece1, [onLea]);

    // Then
    const icons = wrapper.findAllComponents(IconBook);
    expect(icons).toHaveLength(1);
    const words = within(wrapper.element as HTMLElement).getByText(
      "Dans Bibliothèque de Léa",
    );
    expect(
      icons[0].element.compareDocumentPosition(words) &
        Node.DOCUMENT_POSITION_FOLLOWING,
    ).toBeTruthy();
  });

  it("shows the book icon before the absence's words", () => {
    // When
    const wrapper = card(onePiece1, []);

    // Then
    const icons = wrapper.findAllComponents(IconBook);
    expect(icons).toHaveLength(1);
    const words = within(wrapper.element as HTMLElement).getByText(
      "Dans aucune de vos bibliothèques",
    );
    expect(
      icons[0].element.compareDocumentPosition(words) &
        Node.DOCUMENT_POSITION_FOLLOWING,
    ).toBeTruthy();
  });

  it("gives each bookshelf's row its own book icon", () => {
    // Given
    const onSalon: Copy = {
      id: "7a2e3d4c-5b6a-4f70-9c81-2d3e4f5a6b72",
      bookshelf: { id: "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f", name: "Salon" },
    };

    // When
    const wrapper = card(onePiece1, [onSalon, onLea]);

    // Then
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(2);
  });

  it("says no bookshelf when the reader's bookshelves hold no copy", () => {
    // When
    const card = show(onePiece1, []);

    // Then
    const positions = [
      "Eiichirō Oda",
      "Dans aucune de vos bibliothèques",
      "Collection",
    ].map((part) => card.indexOf(part));
    expect(positions).not.toContain(-1);
    expect(positions).toEqual([...positions].sort((a, b) => a - b));
    expect(card.match(/Dans/g)).toHaveLength(1);
  });

  function show(edition: SourceEdition, copies: Copy[] = []) {
    return card(edition, copies).text().replace(/\s+/g, " ");
  }

  function screen(edition: SourceEdition, copies: Copy[] = []) {
    return within(card(edition, copies).element as HTMLElement);
  }

  function card(edition: SourceEdition, copies: Copy[] = []) {
    return mount(SourceEditionCard, {
      props: { edition, copies },
      global: { plugins: [createLibrisI18n()] },
    });
  }
});
