import { describe, expect, it } from "vitest";
import { searchQueryOf } from "./SearchQuery";

describe("searchQueryOf", () => {
  it("reads the words without the spaces at their ends", () => {
    expect(searchQueryOf("  uderzo serpe ")).toBe("uderzo serpe");
  });
});
