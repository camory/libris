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

  function show() {
    return form().text().replace(/\s+/g, " ");
  }

  function form() {
    return mount(EditionForm, {
      props: { isbn13: "9782000000006" },
      global: { plugins: [createLibrisI18n()] },
    });
  }
});
