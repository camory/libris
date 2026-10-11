import { describe, expect, it } from "vitest";
import { nameOrder } from "./nameOrder";

describe("nameOrder", () => {
  it("sorts names ignoring case and accents", () => {
    expect(["Fables", "essais", "Énigmes"].sort(nameOrder("fr"))).toEqual([
      "Énigmes",
      "essais",
      "Fables",
    ]);
  });

  it("finds names that differ only in case or accents equal", () => {
    expect(nameOrder("fr")("Bibliothèque de Léa", "bibliotheque de lea")).toBe(
      0,
    );
  });
});
