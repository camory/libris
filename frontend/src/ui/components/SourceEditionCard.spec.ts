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

  function show(edition: SourceEdition) {
    const wrapper = mount(SourceEditionCard, {
      props: { edition },
      global: { plugins: [createLibrisI18n()] },
    });
    return wrapper.text().replace(/\s+/g, " ");
  }
});
