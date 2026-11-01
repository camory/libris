import { describe, expect, it } from "vitest";
import { languageWord } from "./Language";

describe("languageWord", () => {
  it("answers the French word of a language", () => {
    expect(languageWord("ja", "fr")).toBe("japonais");
  });

  it("answers the code the browser has no word for", () => {
    expect(languageWord("qq", "fr")).toBe("qq");
  });
});
