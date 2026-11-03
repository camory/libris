import { describe, expect, it } from "vitest";
import { onePiece1 } from "../fixture/SourceEditions";
import { editionOf, refusalsOf, type EditionDraft } from "./EditionDraft";

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

describe("refusalsOf", () => {
  it("refuses an empty title", () => {
    // When
    const refusals = check({ ...filled, title: "" });

    // Then
    expect(refusals).toEqual({ title: "missing" });
  });

  it("refuses a title of spaces alone", () => {
    // When
    const refusals = check({ ...filled, title: "   " });

    // Then
    expect(refusals).toEqual({ title: "missing" });
  });

  it("refuses a volume number of 0", () => {
    // When
    const refusals = check({ ...filled, volume: "0" });

    // Then
    expect(refusals).toEqual({ volume: "invalidNumber" });
  });

  it("refuses a year in words", () => {
    // When
    const refusals = check({ ...filled, year: "deux mille" });

    // Then
    expect(refusals).toEqual({ year: "invalidNumber" });
  });

  it("refuses a page count with a decimal", () => {
    // When
    const refusals = check({ ...filled, pages: "1,5" });

    // Then
    expect(refusals).toEqual({ pages: "invalidNumber" });
  });

  it("refuses a number not written in digits", () => {
    // When
    const refusals = check({ ...filled, pages: "1e3" });

    // Then
    expect(refusals).toEqual({ pages: "invalidNumber" });
  });

  it("refuses a word that names no language", () => {
    // When
    const refusals = check({ ...filled, language: "elfique" });

    // Then
    expect(refusals).toEqual({ language: "unknownLanguage" });
  });

  it("refuses a volume number with no series", () => {
    // When
    const refusals = check({ ...filled, series: "" });

    // Then
    expect(refusals).toEqual({ volume: "withoutSeries" });
  });

  it("refuses a volume number with a series of spaces alone", () => {
    // When
    const refusals = check({ ...filled, series: "   " });

    // Then
    expect(refusals).toEqual({ volume: "withoutSeries" });
  });

  it("refuses no field left empty", () => {
    // Given
    const blank: EditionDraft = {
      kind: "MANGA",
      title: "Romance dawn",
      subtitle: "",
      series: " ",
      volume: "",
      collection: "  ",
      publisher: "",
      year: "  ",
      language: "",
      pages: " ",
      summary: "",
    };

    // When
    const refusals = check(blank);

    // Then
    expect(refusals).toEqual({});
  });

  it("refuses nothing in a draft as Libris answered it", () => {
    // When
    const refusals = check({
      ...filled,
      volume: " 1 ",
      year: " 2013 ",
      pages: " 203 ",
      language: " français ",
    });

    // Then
    expect(refusals).toEqual({});
  });

  it("answers every refused field", () => {
    // When
    const refusals = check({
      ...filled,
      title: "",
      year: "0",
      language: "elfique",
    });

    // Then
    expect(refusals).toEqual({
      title: "missing",
      year: "invalidNumber",
      language: "unknownLanguage",
    });
  });

  function check(draft: EditionDraft) {
    return refusalsOf(draft, "fr");
  }
});
