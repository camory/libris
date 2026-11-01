import { BookOpen } from "@lucide/vue";
import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createLibrisI18n } from "../i18n";
import EditionForm from "./EditionForm.vue";

describe("EditionForm", () => {
  it("shows the ISBN under its label", () => {
    // When
    const text = show();

    // Then
    expect(text).toMatch(/ISBN.*9782000000006/);
  });

  it("says no bookshelf of the reader holds a copy", () => {
    // When
    const text = show();

    // Then
    expect(text).toContain("Dans aucune de vos bibliothèques");
  });

  it("draws the stand-in in place of a cover", () => {
    // When
    const wrapper = form();

    // Then
    expect(wrapper.findAllComponents(BookOpen)).toHaveLength(2);
    expect(
      within(wrapper.element as HTMLElement).queryByRole("img"),
    ).toBeNull();
  });

  it("offers the three kinds, Livre chosen", () => {
    // When
    const kinds = screen();

    // Then
    const radios = kinds.getAllByRole<HTMLInputElement>("radio");
    const named = ["Livre", "BD", "Manga"].map((name) =>
      kinds.getByRole<HTMLInputElement>("radio", { name }),
    );
    expect(radios).toHaveLength(3);
    expect(named.map((radio) => radios.indexOf(radio))).toEqual([0, 1, 2]);
    expect(named.map((radio) => radio.checked)).toEqual([true, false, false]);
  });

  it("offers the fields from Titre to Résumé, labelled and empty", () => {
    // When
    const fields = screen();

    // Then
    const textboxes = fields.getAllByRole<HTMLInputElement>("textbox");
    const named = [
      "Titre",
      "Sous-titre",
      "Série",
      "Tome",
      "Collection",
      "Éditeur",
      "Année",
      "Langue",
      "Pages",
      "Résumé",
    ].map((name) => fields.getByRole<HTMLInputElement>("textbox", { name }));
    expect(textboxes).toHaveLength(10);
    expect(named.map((field) => textboxes.indexOf(field))).toEqual([
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
    ]);
    expect(named.map((field) => field.value)).toEqual(Array(10).fill(""));
  });

  it("shows no placeholder", () => {
    // When
    const fields = screen();

    // Then
    expect(fields.queryAllByPlaceholderText(/./)).toEqual([]);
  });

  function show() {
    return form().text().replace(/\s+/g, " ");
  }

  function screen() {
    return within(form().element as HTMLElement);
  }

  function form() {
    return mount(EditionForm, {
      props: { isbn13: "9782000000006" },
      global: { plugins: [createLibrisI18n()] },
    });
  }
});
