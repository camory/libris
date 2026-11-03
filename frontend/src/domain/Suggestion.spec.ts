import { describe, expect, it } from "vitest";
import { suggestionsFor } from "./Suggestion";

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
});
