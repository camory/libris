import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { Book } from "../../domain/Book";
import type { SeriesWithBooks } from "../../domain/Series";
import type { Kind, SourceAuthor } from "../../domain/SourceEdition";
import { createLibrisI18n } from "../i18n";
import SeriesCard from "./SeriesCard.vue";

describe("SeriesCard", () => {
  it("reads a tome alone as itself", () => {
    // Given
    const series = owning("MANGA", [1, 3]);

    // When
    const wrapper = show(series);

    // Then
    expect(words(wrapper.element)).toBe(
      "Tomes possédés 1, 3 Tomes manquants 2",
    );
  });

  it("reads a run of two as both", () => {
    // Given
    const series = owning("MANGA", [1, 2, 5, 6]);

    // When
    const wrapper = show(series);

    // Then
    expect(words(wrapper.element)).toBe(
      "Tomes possédés 1, 2, 5, 6 Tomes manquants 3, 4",
    );
  });

  it("reads a run of three or more as its ends", () => {
    // Given
    const series = owning("MANGA", [1, 2, 3, 7]);

    // When
    const wrapper = show(series);

    // Then
    expect(words(wrapper.element)).toBe(
      "Tomes possédés 1 à 3, 7 Tomes manquants 4 à 6",
    );
  });

  it("has no missing row when no tome is missing", () => {
    // Given
    const series = owning("MANGA", [1, 2]);

    // When
    const wrapper = show(series);

    // Then
    expect(words(wrapper.element)).toBe("Tomes possédés 1, 2");
  });

  it("says albums for a BD", () => {
    // Given
    const series = owning("BD", [1, 3]);

    // When
    const wrapper = show(series);

    // Then
    expect(words(wrapper.element)).toBe(
      "Albums possédés 1, 3 Albums manquants 2",
    );
  });

  it("says tomes when the editions differ in kind", () => {
    // Given
    const series = owning(null, [1, 3]);

    // When
    const wrapper = show(series);

    // Then
    expect(words(wrapper.element)).toBe(
      "Tomes possédés 1, 3 Tomes manquants 2",
    );
  });

  it("names the authors of the series", () => {
    // Given
    const series = owning("BD", [1, 2], [goscinny, uderzo]);

    // When
    const wrapper = show(series);

    // Then
    expect(words(wrapper.element)).toBe(
      "Albums possédés 1, 2 Auteurs René Goscinny, Albert Uderzo",
    );
  });

  it("says auteur for one author", () => {
    // Given
    const series = owning("BD", [1, 2], [goscinny]);

    // When
    const wrapper = show(series);

    // Then
    expect(words(wrapper.element)).toBe(
      "Albums possédés 1, 2 Auteur René Goscinny",
    );
  });

  it("names the publishers after the authors", () => {
    // Given
    const series = owning("BD", [1, 2], [goscinny], ["Hachette", "Orion"]);

    // When
    const wrapper = show(series);

    // Then
    expect(words(wrapper.element)).toBe(
      "Albums possédés 1, 2 Auteur René Goscinny Éditeurs Hachette, Orion",
    );
  });
});

const goscinny: SourceAuthor = { name: "René Goscinny", role: "WRITER" };
const uderzo: SourceAuthor = { name: "Albert Uderzo", role: "ARTIST" };

function show(series: SeriesWithBooks) {
  return mount(SeriesCard, {
    props: { series },
    global: { plugins: [createLibrisI18n()] },
  });
}

function words(element: Element) {
  const texts = document.createTreeWalker(element, NodeFilter.SHOW_TEXT);
  const words: string[] = [];
  while (texts.nextNode()) {
    const text = texts.currentNode.textContent?.trim();
    if (text) words.push(text);
  }
  return words.join(" ");
}

function owning(
  kind: Kind | null,
  tomes: number[],
  authors: SourceAuthor[] = [],
  publishers: string[] = [],
): SeriesWithBooks {
  return {
    id: "5a6b7c8d-9e0f-4a1b-8c2d-3e4f5a6b7c8d",
    name: "One piece",
    volumeCount: tomes.length,
    coverUrl: null,
    kind,
    books: tomes.map((tome, i) =>
      book(kind ?? "MANGA", tome, authors, publishers[i] ?? null),
    ),
  };
}

function book(
  kind: Kind,
  volumeNumber: number,
  authors: SourceAuthor[],
  publisher: string | null,
): Book {
  return {
    id: crypto.randomUUID(),
    isbn13: null,
    kind,
    title: `One piece ${volumeNumber}`,
    subtitle: null,
    authors,
    series: { name: "One piece", volumeNumber },
    collection: null,
    publisher,
    publicationYear: null,
    language: null,
    pageCount: null,
    summary: null,
    coverUrl: null,
    copies: [],
  };
}
