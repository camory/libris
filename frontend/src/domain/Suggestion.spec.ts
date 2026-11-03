import { describe, expect, it } from "vitest";
import { suggestionsFor, type Suggestion } from "./Suggestion";

describe("suggestionsFor", () => {
  it("suggests the names that contain the text, ignoring case", () => {
    expect(suggestionsFor("PIE", ["One piece", "Astérix"], "fr")).toEqual([
      { before: "One ", matched: "pie", after: "ce" },
    ]);
  });

  it("ignores the accents", () => {
    expect(suggestionsFor("pie", ["Épiés"], "fr")).toEqual([
      { before: "É", matched: "pié", after: "s" },
    ]);
  });

  it("ignores the spaces at the text's ends", () => {
    expect(suggestionsFor(" pie ", ["One piece"], "fr")).toEqual([
      { before: "One ", matched: "pie", after: "ce" },
    ]);
  });

  it("sorts them by name in the reader's language", () => {
    expect(
      suggestionsFor(
        "pi",
        ["Pièces détachées", "Épiés", "One piece"],
        "fr",
      ).map(nameOf),
    ).toEqual(["Épiés", "One piece", "Pièces détachées"]);
  });

  it("keeps the first five", () => {
    expect(
      suggestionsFor(
        "pie",
        [
          "Pierrot",
          "Spirou et Fantasio",
          "Piège",
          "One piece",
          "Les Pieds Nickelés",
          "Astérix",
          "Épiés",
          "Pièces détachées",
        ],
        "fr",
      ).map(nameOf),
    ).toEqual([
      "Épiés",
      "Les Pieds Nickelés",
      "One piece",
      "Pièces détachées",
      "Piège",
    ]);
  });

  it("suggests nothing for a blank text", () => {
    expect(suggestionsFor("   ", ["One piece"], "fr")).toEqual([]);
  });

  it("keeps an accent written as a mark of its own in its part", () => {
    expect(suggestionsFor("pieg", ["Pie\u0300ge"], "fr")).toEqual([
      { before: "", matched: "Pie\u0300g", after: "e" },
    ]);
  });

  it("folds the text as it folds the name, character by character", () => {
    expect(
      suggestionsFor(
        "\u039f\u0394\u039f\u03a3",
        ["\u039f\u0394\u039f\u03a3"],
        "fr",
      ),
    ).toEqual([{ before: "", matched: "\u039f\u0394\u039f\u03a3", after: "" }]);
  });
});

function nameOf(suggestion: Suggestion) {
  return suggestion.before + suggestion.matched + suggestion.after;
}
