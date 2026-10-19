import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { Run } from "../../domain/Run";
import type { Kind } from "../../domain/SourceEdition";
import { createLibrisI18n } from "../i18n";
import GapRow from "./GapRow.vue";

describe("GapRow", () => {
  it("names a run of missing tomes", () => {
    // When
    const wrapper = show({ first: 26, last: 35 }, "MANGA");

    // Then
    expect(words(wrapper.element)).toBe("Tomes 26 à 35 Manquants");
  });

  it("names a missing tome alone", () => {
    // When
    const wrapper = show({ first: 4, last: 4 }, "MANGA");

    // Then
    expect(words(wrapper.element)).toBe("Tome 4 Manquant");
  });

  it("says album for a BD", () => {
    // When
    const wrapper = show({ first: 3, last: 3 }, "BD");

    // Then
    expect(words(wrapper.element)).toBe("Album 3 Manquant");
  });

  it("says tome when the editions differ in kind", () => {
    // When
    const wrapper = show({ first: 4, last: 4 }, null);

    // Then
    expect(words(wrapper.element)).toBe("Tome 4 Manquant");
  });
});

function show(gap: Run, kind: Kind | null) {
  return mount(GapRow, {
    props: { gap, kind },
    global: { plugins: [createLibrisI18n()] },
  });
}

function words(element: Element) {
  const texts = document.createTreeWalker(element, NodeFilter.SHOW_TEXT);
  const words: string[] = [];
  while (texts.nextNode()) {
    const text = texts.currentNode.textContent?.trim();
    if (text) words.push(text);
  }
  return words.join(" ");
}
