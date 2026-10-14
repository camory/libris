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
import ReaderChoice from "./ReaderChoice.vue";

describe("ReaderChoice", () => {
  it("offers a row per reader, each a button", () => {
    // When
    const { screen } = open();

    // Then
    const rows = within(
      screen.getByRole("list", { name: "Choisir un lecteur" }),
    ).getAllByRole("listitem");
    expect(
      rows.map((row) =>
        within(row)
          .getAllByRole("button")
          .map((button) => button.textContent?.trim()),
      ),
    ).toEqual([["Hugo"], ["Marc"]]);
  });

  it("makes the tapped reader's row the form", async () => {
    // Given
    const { screen } = open();

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Hugo" }));

    // Then
    const row = rowOf(screen, "Hugo");
    expect(row.textContent).toContain("Hugo");
    expect(
      within(row).getByRole("button", { name: "Propriétaire", pressed: false }),
    ).toBeDefined();
    expect(within(row).getByRole("button", { name: "Ajouter" })).toBeDefined();
    expect(screen.queryByRole("button", { name: "Hugo" })).toBeNull();
  });

  function rowOf(screen: Screen, displayName: string) {
    return within(screen.getByRole("list", { name: "Choisir un lecteur" }))
      .getAllByRole("listitem")
      .find((row) => row.textContent?.includes(displayName))!;
  }

  function open(adding = false) {
    const wrapper = mount(ReaderChoice, {
      props: { readers: [hugo, marc], adding },
      global: { plugins: [createLibrisI18n()] },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
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
