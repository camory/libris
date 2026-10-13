import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { KnownReader } from "../../domain/KnownReader";
import { createLibrisI18n } from "../i18n";
import NewMemberRow from "./NewMemberRow.vue";

describe("NewMemberRow", () => {
  it("offers the readers after Choisir un lecteur", () => {
    // When
    const { screen } = open();

    // Then
    const choice = screen.getByRole("combobox", { name: "Lecteur" });
    expect(
      within(choice).getByRole("option", { selected: true }).textContent,
    ).toBe("Choisir un lecteur");
    expect(
      within(choice)
        .getAllByRole("option")
        .map((option) => option.textContent),
    ).toEqual(["Choisir un lecteur", "Hugo", "Marc"]);
  });

  function open(adding = false) {
    const wrapper = mount(NewMemberRow, {
      props: { readers: [hugo, marc], adding },
      global: { plugins: [createLibrisI18n()] },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }
});

const hugo: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000004",
  displayName: "Hugo",
};

const marc: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000005",
  displayName: "Marc",
};
