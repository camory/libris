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
