import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { Book } from "../../domain/Book";
import {
  asterixEtSesAmis,
  asterixLeGaulois,
  lePetitPrince,
  romanceDawn,
} from "../../fixture/Books";
import { createLibrisI18n } from "../i18n";
import CatalogueRow from "./CatalogueRow.vue";

describe("CatalogueRow", () => {
  it("shows the title", () => {
    // When
    const { screen } = show(romanceDawn);

    // Then
    expect(screen.getByText("Romance dawn")).toBeDefined();
  });

  it("shows the series and the tome over the title", () => {
    // When
    const { screen } = show(romanceDawn);

    // Then
    expect(screen.getByText("One piece · tome 1")).toBeDefined();
  });

  it("says tome for a BD too", () => {
    // When
    const { screen } = show(asterixLeGaulois);

    // Then
    expect(screen.getByText("Astérix · tome 1")).toBeDefined();
  });

  it("shows the series alone when the edition has no tome", () => {
    // When
    const { screen } = show(asterixEtSesAmis);

    // Then
    expect(screen.getByText("Astérix")).toBeDefined();
  });

  it("shows no overline when the edition has no series", () => {
    // When
    const { wrapper } = show(lePetitPrince);

    // Then
    expect(normalised(wrapper.element)).toMatch(/^Le Petit Prince/);
  });

  function show(book: Book) {
    const wrapper = mount(CatalogueRow, {
      props: { book },
      global: { plugins: [createLibrisI18n()] },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }

  function normalised(element: Element) {
    return element.textContent?.replace(/\s+/g, " ").trim() ?? "";
  }
});
