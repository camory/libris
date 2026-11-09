import { describe, expect, it } from "vitest";
import { nameStatus } from "./NameStatus";

describe("nameStatus", () => {
  const names = ["One piece", "Astérix"];

  it("says new for a name the house does not hold", () => {
    expect(nameStatus("One pie", names, "fr")).toBe("new");
  });

  it("says existing for a name of the house, ignoring case", () => {
    expect(nameStatus("ONE PIECE", names, "fr")).toBe("existing");
  });

  it("ignores the accents", () => {
    expect(nameStatus("asterix", names, "fr")).toBe("existing");
  });

  it("ignores the spaces at the text's ends", () => {
    expect(nameStatus(" one piece ", names, "fr")).toBe("existing");
  });

  it("says nothing for a blank text", () => {
    expect(nameStatus("   ", names, "fr")).toBeNull();
  });

  it("says nothing while the names are unknown", () => {
    expect(nameStatus("One piece", null, "fr")).toBeNull();
  });
});
