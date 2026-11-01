import { describe, expect, it } from "vitest";
import { onePiece1 } from "../fixture/SourceEditions";
import { editionOf, type EditionDraft } from "./EditionDraft";

const filled: EditionDraft = {
  kind: "MANGA",
  title: "Romance dawn",
  subtitle: "à l'aube d'une grande aventure",
  series: "One piece",
  volume: "1",
  collection: "Shonen manga",
  publisher: "Glénat",
  year: "2013",
  language: "français",
  pages: "203",
  summary: "",
};

describe("editionOf", () => {
  it("reads a filled draft as the edition it holds", () => {
    // When
    const edition = read(filled);

    // Then
    expect(edition).toEqual(onePiece1);
  });

  it("removes the spaces at the ends of each text", () => {
    // Given
    const spaced = Object.fromEntries(
      Object.entries(filled).map(([field, value]) => [
        field,
        field === "kind" ? value : `  ${value} `,
      ]),
    ) as unknown as EditionDraft;

    // When
    const edition = read({ ...spaced, summary: " Un.\nDeux. " });

    // Then
    expect(edition).toEqual({ ...onePiece1, summary: "Un.\nDeux." });
  });

  it("reads a field of spaces alone as no value", () => {
    // Given
    const blank: EditionDraft = {
      kind: "MANGA",
      title: "Romance dawn",
      subtitle: "  ",
      series: " ",
      volume: "1",
      collection: "  ",
      publisher: " ",
      year: "  ",
      language: " ",
      pages: "   ",
      summary: " \n ",
    };

    // When
    const edition = read(blank);

    // Then
    expect(edition).toEqual({
      ...onePiece1,
      subtitle: null,
      series: null,
      collection: null,
      publisher: null,
      publicationYear: null,
      language: null,
      pageCount: null,
      summary: null,
    });
  });

  function read(draft: EditionDraft) {
    return editionOf(onePiece1.isbn13, onePiece1.authors, draft, "fr");
  }
});
