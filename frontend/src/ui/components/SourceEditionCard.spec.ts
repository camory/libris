import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { nextTick } from "vue";
import type { Copy } from "../../domain/Copy";
import type { CoverCandidate } from "../../domain/Cover";
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

const inventaire: CoverCandidate = {
  source: "inventaire.io",
  url: "https://inventaire.io/img/entities/480x600/34d6e7d99cec5b0922b9eccfeb03748ab2b4db99",
};

const openLibrary: CoverCandidate = {
  source: "Open Library",
  url: "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg?default=false",
};

const bnf: CoverCandidate = {
  source: "BnF",
  url: "https://catalogue.bnf.fr/couverture?&appName=NE&idArk=ark:/12148/cb43636708p&couverture=1",
};

const libris: CoverCandidate = {
  source: "Libris",
  url: "/api/v1/covers/5b1c2a4f0e6d8a9b3c7e1f2d4a6b8c0e9f1a3b5c7d9e0f2a4b6c8d0e1f3a5b7c",
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

  it("shows the first cover the sources offer, named after the ouvrage", () => {
    // When
    const covers = screen(
      onePiece1,
      [],
      [inventaire, openLibrary],
    ).getAllByRole("img");

    // Then
    expect(covers).toHaveLength(1);
    expect(covers[0].getAttribute("src")).toBe(inventaire.url);
    expect(covers[0].getAttribute("alt")).toBe("Couverture de Romance dawn");
  });

  it("shows a book icon when the sources offer no candidate", () => {
    // When
    const wrapper = card(onePiece1, [], []);

    // Then
    expect(
      within(wrapper.element as HTMLElement).queryAllByRole("img"),
    ).toEqual([]);
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(2);
  });

  it("shows the next candidate when the first does not load", async () => {
    // Given
    const wrapper = card(onePiece1, [], [inventaire, openLibrary, bnf]);
    const shown = within(wrapper.element as HTMLElement);

    // When
    shown.getByRole("img").dispatchEvent(new Event("error"));
    await nextTick();

    // Then
    const cover = shown.getByRole("img");
    expect(cover.getAttribute("src")).toBe(openLibrary.url);
    expect(cover.getAttribute("alt")).toBe("Couverture de Romance dawn");
  });

  it("gives out no source when it shows the stand-in", () => {
    // When
    const wrapper = card(onePiece1, [], []);

    // Then
    expect(givenOut(wrapper)).toBeNull();
  });

  it("gives out the source of the first cover it shows", () => {
    // When
    const wrapper = card(onePiece1, [], [inventaire, openLibrary, bnf]);

    // Then
    expect(givenOut(wrapper)).toBe("inventaire.io");
  });

  it("gives out the next source when the shown cover does not load", async () => {
    // Given
    const wrapper = card(onePiece1, [], [inventaire, openLibrary, bnf]);
    const shown = within(wrapper.element as HTMLElement);

    // When
    shown.getByRole("img").dispatchEvent(new Event("error"));
    await nextTick();

    // Then
    expect(givenOut(wrapper)).toBe("Open Library");
  });

  it("gives out the source of the dot tapped", async () => {
    // Given
    const wrapper = card(onePiece1, [], [inventaire, openLibrary, bnf]);
    const shown = within(wrapper.element as HTMLElement);

    // When
    shown.getByRole("button", { name: "Couverture BnF" }).click();
    await nextTick();

    // Then
    expect(givenOut(wrapper)).toBe("BnF");
  });

  it("gives out no source once no candidate loads", async () => {
    // Given
    const wrapper = card(onePiece1, [], [inventaire, openLibrary, bnf]);
    const shown = within(wrapper.element as HTMLElement);

    // When
    for (const image of shown.getAllByRole("img", { hidden: true })) {
      image.dispatchEvent(new Event("error"));
      await nextTick();
    }

    // Then
    expect(givenOut(wrapper)).toBeNull();
  });

  it("offers one dot per candidate, named after its source", () => {
    // When
    const card = screen(onePiece1, [], [inventaire, openLibrary, bnf]);

    // Then
    expect(labels(card)).toEqual([
      "Couverture inventaire.io",
      "Couverture Open Library",
      "Couverture BnF",
    ]);
  });

  it("takes the dot away from a candidate that does not load", async () => {
    // Given
    const card = screen(onePiece1, [], [inventaire, openLibrary, bnf]);

    // When
    card
      .getAllByRole("img", { hidden: true })[2]
      .dispatchEvent(new Event("error"));
    await nextTick();

    // Then
    expect(labels(card)).toEqual([
      "Couverture inventaire.io",
      "Couverture Open Library",
    ]);
    expect(card.getByRole("img").getAttribute("src")).toBe(inventaire.url);
  });

  it("presses the dot of the shown cover", () => {
    // When
    const card = screen(onePiece1, [], [inventaire, openLibrary, bnf]);

    // Then
    expect(pressed(card)).toEqual(["true", "false", "false"]);
  });

  it("shows the cover of the dot tapped, with its name", async () => {
    // Given
    const wrapper = card(onePiece1, [], [inventaire, openLibrary, bnf]);
    const shown = within(wrapper.element as HTMLElement);

    // When
    shown.getByRole("button", { name: "Couverture Open Library" }).click();
    await nextTick();

    // Then
    expect(shown.getByRole("img").getAttribute("src")).toBe(openLibrary.url);
    const text = wrapper.text();
    expect(text).toContain("Open Library");
    expect(text).not.toContain("inventaire.io");
    expect(pressed(shown)).toEqual(["false", "true", "false"]);
  });

  it("shows the first cover still loading when the tapped one does not load", async () => {
    // Given
    const card = screen(onePiece1, [], [inventaire, openLibrary, bnf]);
    card.getByRole("button", { name: "Couverture BnF" }).click();
    await nextTick();

    // When
    card.getByRole("img").dispatchEvent(new Event("error"));
    await nextTick();

    // Then
    expect(card.getByRole("img").getAttribute("src")).toBe(inventaire.url);
    expect(pressed(card)).toEqual(["true", "false"]);
  });

  it("shows the book icon, no dot and no name when no candidate loads", async () => {
    // Given
    const wrapper = card(onePiece1, [], [inventaire, openLibrary, bnf]);
    const shown = within(wrapper.element as HTMLElement);
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(1);

    // When
    for (const image of shown.getAllByRole("img", { hidden: true })) {
      image.dispatchEvent(new Event("error"));
      await nextTick();
    }

    // Then
    expect(shown.queryAllByRole("img")).toEqual([]);
    expect(dots(shown)).toEqual([]);
    const text = wrapper.text();
    for (const source of ["inventaire.io", "Open Library", "BnF"]) {
      expect(text).not.toContain(source);
    }
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(2);
  });

  it("names the source of the shown cover, and no other", () => {
    // When
    const card = show(onePiece1, [], [inventaire, openLibrary, bnf]);

    // Then
    expect(card.match(/inventaire\.io/g)).toHaveLength(1);
    expect(card).not.toContain("Sources");
    expect(card).not.toContain("Open Library");
    expect(card).not.toContain("BnF");
  });

  it("draws no dot under the cover of an edition the house holds", () => {
    // When
    const card = screen(onePiece1, [], [libris], true);

    // Then
    expect(dots(card)).toEqual([]);
  });

  it("names no source under the cover of an edition the house holds", () => {
    // When
    const card = show(onePiece1, [], [libris], true);

    // Then
    expect(card).not.toContain("Libris");
  });

  it("shows the house's cover for an edition the house holds", () => {
    // When
    const covers = screen(onePiece1, [], [libris], true).getAllByRole("img");

    // Then
    expect(covers).toHaveLength(1);
    expect(covers[0].getAttribute("src")).toBe(libris.url);
    expect(covers[0].getAttribute("alt")).toBe("Couverture de Romance dawn");
  });

  it("offers a dot for a candidate named Libris when the house does not hold the edition", () => {
    // When
    const card = screen(onePiece1, [], [libris]);

    // Then
    expect(labels(card)).toEqual(["Couverture Libris"]);
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
    const wrapper = card(onePiece1, [onLea], [inventaire]);

    // Then
    const icons = wrapper.findAllComponents(IconBook);
    expect(icons).toHaveLength(1);
    const words = within(wrapper.element as HTMLElement).getByText(
      "Dans Bibliothèque de Léa",
    );
    expect(precedes(icons[0].element, words)).toBe(true);
  });

  it("shows the book icon before the absence's words", () => {
    // When
    const wrapper = card(onePiece1, [], [inventaire]);

    // Then
    const icons = wrapper.findAllComponents(IconBook);
    expect(icons).toHaveLength(1);
    const words = within(wrapper.element as HTMLElement).getByText(
      "Dans aucune de vos bibliothèques",
    );
    expect(precedes(icons[0].element, words)).toBe(true);
  });

  it("gives each bookshelf's row its own book icon", () => {
    // Given
    const onSalon: Copy = {
      id: "7a2e3d4c-5b6a-4f70-9c81-2d3e4f5a6b72",
      bookshelf: { id: "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f", name: "Salon" },
    };

    // When
    const wrapper = card(onePiece1, [onSalon, onLea], [inventaire]);

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

  function precedes(first: Node, second: Node) {
    return Boolean(
      first.compareDocumentPosition(second) & Node.DOCUMENT_POSITION_FOLLOWING,
    );
  }

  function dots(card: ReturnType<typeof screen>) {
    return card.queryAllByRole("button", { name: /^Couverture / });
  }

  function labels(card: ReturnType<typeof screen>) {
    return dots(card).map((dot) => dot.getAttribute("aria-label"));
  }

  function pressed(card: ReturnType<typeof screen>) {
    return dots(card).map((dot) => dot.getAttribute("aria-pressed"));
  }

  function givenOut(wrapper: ReturnType<typeof card>) {
    return wrapper.emitted<[string | null]>("coverSource")?.at(-1)?.[0];
  }

  function show(
    edition: SourceEdition,
    copies: Copy[] = [],
    covers: CoverCandidate[] = [],
    held = false,
  ) {
    return card(edition, copies, covers, held).text().replace(/\s+/g, " ");
  }

  function screen(
    edition: SourceEdition,
    copies: Copy[] = [],
    covers: CoverCandidate[] = [],
    held = false,
  ) {
    return within(card(edition, copies, covers, held).element as HTMLElement);
  }

  function card(
    edition: SourceEdition,
    copies: Copy[] = [],
    covers: CoverCandidate[] = [],
    held = false,
  ) {
    return mount(SourceEditionCard, {
      props: { edition, copies, covers, held },
      global: { plugins: [createLibrisI18n()] },
    });
  }
});
