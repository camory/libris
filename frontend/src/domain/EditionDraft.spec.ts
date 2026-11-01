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

  function read(draft: EditionDraft) {
    return editionOf(onePiece1.isbn13, onePiece1.authors, draft, "fr");
  }
});
