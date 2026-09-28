import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { Book } from "../../domain/Book";
import { asterixLeGaulois, romanceDawn } from "../../fixture/Books";
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

  function show(book: Book) {
    const wrapper = mount(CatalogueRow, {
      props: { book },
      global: { plugins: [createLibrisI18n()] },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }
});
