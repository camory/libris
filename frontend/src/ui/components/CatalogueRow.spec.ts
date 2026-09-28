import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { Book } from "../../domain/Book";
import {
  asterixEtSesAmis,
  asterixLeGaulois,
  lePetitPrince,
  romanceDawn,
  salon,
} from "../../fixture/Books";
import { createLibrisI18n } from "../i18n";
import IconBook from "./icons/IconBook.vue";
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

  it("names the authors on one line, separated by commas", () => {
    // When
    const { screen } = show(asterixLeGaulois);

    // Then
    expect(screen.getByText("René Goscinny, Albert Uderzo")).toBeDefined();
  });

  it("names an author of two roles once", () => {
    // When
    const { screen } = show(romanceDawn);

    // Then
    expect(screen.getByText("Eiichirō Oda")).toBeDefined();
  });

  it("lists the bookshelves holding a copy, separated by commas", () => {
    // Given
    const onTwoBookshelves: Book = {
      ...asterixLeGaulois,
      copies: [
        ...asterixLeGaulois.copies,
        { id: "f2a0bf1e-d34c-4bf8-94a9-afb0c1d2e3fa", bookshelf: salon },
      ],
    };

    // When
    const { screen } = show(onTwoBookshelves);

    // Then
    expect(screen.getByText("Bibliothèque de Léa, Salon")).toBeDefined();
  });

  it("counts the copies of a bookshelf holding more than one", () => {
    // When
    const { screen } = show(romanceDawn);

    // Then
    expect(
      screen.getByText("Bibliothèque de Léa · 2 exemplaires, Salon"),
    ).toBeDefined();
  });

  it("shows the cover, named after the ouvrage", () => {
    // When
    const { screen } = show(romanceDawn);

    // Then
    const cover = screen.getByRole("img", {
      name: "Couverture de Romance dawn",
    });
    expect(cover.getAttribute("src")).toBe(romanceDawn.coverUrl);
  });

  it("shows a book icon when the edition has no cover", () => {
    // When
    const { wrapper, screen } = show(lePetitPrince);

    // Then
    expect(screen.queryByRole("img")).toBeNull();
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(1);
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
