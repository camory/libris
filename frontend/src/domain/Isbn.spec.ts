import { describe, expect, it } from "vitest";
import { Isbn } from "./Isbn";

describe("Isbn", () => {
  it("keeps thirteen digits that are an ISBN-13", () => {
    expect(Isbn.of("9782723488525")?.digits).toBe("9782723488525");
  });

  it("drops hyphens and spaces wherever they sit", () => {
    expect(Isbn.of("978-2-7234-8852-5")?.digits).toBe("9782723488525");
    expect(Isbn.of("978 2 7234 8852 5")?.digits).toBe("9782723488525");
  });

  it("refuses thirteen digits whose check digit is wrong", () => {
    expect(Isbn.of("9782723488526")).toBeNull();
  });

  it("refuses a text of the wrong length", () => {
    expect(Isbn.of("978272348852")).toBeNull();
  });

  it("converts the old ten", () => {
    expect(Isbn.of("2723488527")?.digits).toBe("9782723488525");
    expect(Isbn.of("2-7234-8852-7")?.digits).toBe("9782723488525");
  });

  it("converts a ten whose check digit is X, in either case", () => {
    expect(Isbn.of("080442957X")?.digits).toBe("9780804429573");
    expect(Isbn.of("080442957x")?.digits).toBe("9780804429573");
  });

  it("refuses a ten whose check digit is wrong", () => {
    expect(Isbn.of("2723488521")).toBeNull();
  });

  it("refuses a ten whose check digit is not a digit nor X", () => {
    expect(Isbn.of("000000000\t")).toBeNull();
    expect(Isbn.of("000000000\n")).toBeNull();
    expect(Isbn.of("000000000\u00a0")).toBeNull();
  });

  it("refuses a thirteen-digit EAN with another prefix", () => {
    expect(Isbn.of("4006381333931")).toBeNull();
  });

  it("refuses a text that is not digits", () => {
    expect(Isbn.of("978-2-7234-8852-X")).toBeNull();
    expect(Isbn.of("")).toBeNull();
  });
});
