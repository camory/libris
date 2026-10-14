import { within } from "@testing-library/dom";
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

  function open(adding = false) {
    const wrapper = mount(ReaderChoice, {
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
