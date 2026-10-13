import {
  fireEvent,
  within,
  type BoundFunctions,
  type queries,
} from "@testing-library/dom";
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

  it("adds the chosen reader as invité", async () => {
    // Given
    const { wrapper, screen } = open();
    await choose(screen, "Hugo");

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Ajouter" }));

    // Then
    expect(wrapper.emitted("add")).toEqual([[hugo.id, "VIEWER"]]);
  });

  it("offers no Ajouter until a reader is chosen", () => {
    // When
    const { screen } = open();

    // Then
    expect(screen.queryByRole("button", { name: "Ajouter" })).toBeNull();
  });

  it("adds as propriétaire with the crown on", async () => {
    // Given
    const { wrapper, screen } = open();
    await fireEvent.click(screen.getByRole("button", { name: "Propriétaire" }));
    await choose(screen, "Hugo");

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Ajouter" }));

    // Then
    expect(wrapper.emitted("add")).toEqual([[hugo.id, "OWNER"]]);
  });

  function open(adding = false) {
    const wrapper = mount(NewMemberRow, {
      props: { readers: [hugo, marc], adding },
      global: { plugins: [createLibrisI18n()] },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }

  async function choose(screen: Screen, displayName: string) {
    const choice = screen.getByRole<HTMLSelectElement>("combobox", {
      name: "Lecteur",
    });
    within(choice).getByRole<HTMLOptionElement>("option", {
      name: displayName,
    }).selected = true;
    await fireEvent.change(choice);
  }
});

type Screen = BoundFunctions<typeof queries>;

const hugo: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000004",
  displayName: "Hugo",
};

const marc: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000005",
  displayName: "Marc",
};
