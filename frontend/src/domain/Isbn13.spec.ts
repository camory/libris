import { describe, expect, it } from "vitest";
import { isbn13Of } from "./Isbn13";

describe("isbn13Of", () => {
  it("keeps thirteen digits that are an ISBN-13", () => {
    expect(isbn13Of("9782723488525")).toBe("9782723488525");
  });

  it("drops hyphens and spaces wherever they sit", () => {
    expect(isbn13Of("978-2-7234-8852-5")).toBe("9782723488525");
    expect(isbn13Of("978 2 7234 8852 5")).toBe("9782723488525");
  });

  it("refuses thirteen digits whose check digit is wrong", () => {
    expect(isbn13Of("9782723488526")).toBeNull();
  });

  it("refuses a text of the wrong length", () => {
    expect(isbn13Of("978272348852")).toBeNull();
  });

  it("converts the old ten", () => {
    expect(isbn13Of("2723488527")).toBe("9782723488525");
    expect(isbn13Of("2-7234-8852-7")).toBe("9782723488525");
  });

  it("converts a ten whose check digit is X, in either case", () => {
    expect(isbn13Of("080442957X")).toBe("9780804429573");
    expect(isbn13Of("080442957x")).toBe("9780804429573");
  });

  it("refuses a ten whose check digit is wrong", () => {
    expect(isbn13Of("2723488521")).toBeNull();
  });
});
