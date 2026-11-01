import { describe, expect, it } from "vitest";
import { languageWord } from "./Language";

describe("languageWord", () => {
  it("answers the French word of a language", () => {
    expect(languageWord("ja", "fr")).toBe("japonais");
  });
});
