import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { SourceEdition } from "../../domain/SourceEdition";
import { onePiece1 } from "../../fixture/SourceEditions";
import { createLibrisI18n } from "../i18n";
import SourceEditionCard from "./SourceEditionCard.vue";

describe("SourceEditionCard", () => {
  it("shows the series and the volume, the title and the subtitle", () => {
    // When
    const card = show(onePiece1);

    // Then
    expect(card).toContain("One piece · tome 1");
    expect(card).toContain("Romance dawn");
    expect(card).toContain("à l'aube d'une grande aventure");
  });

  it("shows one line per author, with the French words of its roles", () => {
    // When
    const card = show(onePiece1);

    // Then
    expect(card).toContain("Eiichirō Oda · scénario, dessin");
    expect(card.match(/Eiichirō Oda/g)).toHaveLength(1);
  });

  it("shows one row per field, in the order of the card", () => {
    // When
    const card = show(onePiece1);

    // Then
    const parts = [
      "Collection",
      "Shonen manga",
      "Éditeur",
      "Glénat",
      "Année",
      "2013",
      "Langue",
      "français",
      "Pages",
      "203",
      "ISBN",
      "9782723488525",
    ];
    const positions = parts.map((part) => card.indexOf(part));
    expect(positions).not.toContain(-1);
    expect(positions).toEqual([...positions].sort((a, b) => a - b));
  });

  function show(edition: SourceEdition) {
    const wrapper = mount(SourceEditionCard, {
      props: { edition },
      global: { plugins: [createLibrisI18n()] },
    });
    return wrapper.text().replace(/\s+/g, " ");
  }
});
