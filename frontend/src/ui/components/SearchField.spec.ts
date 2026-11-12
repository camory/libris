import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createLibrisI18n } from "../i18n";
import SearchField from "./SearchField.vue";

describe("SearchField", () => {
  it("is a search box named for the catalogue", () => {
    // When
    const screen = within(field().element);

    // Then
    expect(
      screen.getAllByRole("searchbox", {
        name: "Rechercher dans le catalogue",
      }),
    ).toHaveLength(1);
  });

  it("shows an example of the words", () => {
    // When
    const screen = within(field().element);

    // Then
    expect(screen.getByRole("searchbox").getAttribute("placeholder")).toBe(
      "Titre, série, auteur, ISBN",
    );
  });
});

function field() {
  return mount(SearchField, {
    props: { modelValue: "" },
    global: { plugins: [createLibrisI18n()] },
  });
}
