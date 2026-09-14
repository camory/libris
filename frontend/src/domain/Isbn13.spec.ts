import { describe, expect, it } from "vitest";
import { isbn13Of } from "./Isbn13";

describe("isbn13Of", () => {
  it("keeps thirteen digits that are an ISBN-13", () => {
    expect(isbn13Of("9782723488525")).toBe("9782723488525");
  });
});
