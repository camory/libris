import { describe, expect, it } from "vitest";
import { searchQueryOf } from "./SearchQuery";

describe("searchQueryOf", () => {
  it("reads the words without the spaces at their ends", () => {
    expect(searchQueryOf("  uderzo serpe ")).toBe("uderzo serpe");
  });

  it("asks nothing under two characters", () => {
    expect(searchQueryOf(" g ")).toBeNull();
  });

  it("counts a character of two code units as one", () => {
    expect(searchQueryOf(" 𠮷 ")).toBeNull();
  });

  it("asks from two characters on", () => {
    expect(searchQueryOf(" go ")).toBe("go");
  });
});
