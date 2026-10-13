import { describe, expect, it } from "vitest";
import { isBookshelfName } from "./Bookshelf";

describe("isBookshelfName", () => {
  it("refuses a blank name", () => {
    expect(isBookshelfName("   ")).toBe(false);
  });

  it("accepts a name", () => {
    expect(isBookshelfName("Salon")).toBe(true);
  });
});
