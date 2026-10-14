import {
  fireEvent,
  within,
  type BoundFunctions,
  type queries,
} from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { KnownReader } from "../../domain/KnownReader";
import { createLibrisI18n } from "../i18n";
import ReaderChoice from "./ReaderChoice.vue";

describe("ReaderChoice", () => {
  afterEach(() => {
    Reflect.deleteProperty(HTMLElement.prototype, "scrollIntoView");
  });

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
    const row = formRow(screen);
    expect(within(row).getByText("Hugo")).toBeDefined();
    expect(
      within(row).getByRole("button", { name: "Propriétaire", pressed: false }),
    ).toBeDefined();
    expect(within(row).getByRole("button", { name: "Ajouter" })).toBeDefined();
    expect(screen.queryByRole("button", { name: "Hugo" })).toBeNull();
  });

  it("offers no crown and no Ajouter until a reader is tapped", () => {
    // When
    const { screen } = open();

    // Then
    expect(screen.queryByRole("button", { name: "Propriétaire" })).toBeNull();
    expect(screen.queryByRole("button", { name: "Ajouter" })).toBeNull();
  });

  it("adds the tapped reader as invité", async () => {
    // Given
    const { wrapper, screen } = open();
    await fireEvent.click(screen.getByRole("button", { name: "Hugo" }));

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Ajouter" }));

    // Then
    expect(wrapper.emitted("add")).toEqual([[hugo.id, "VIEWER"]]);
  });

  it("presses the crown once tapped", async () => {
    // Given
    const { screen } = open();
    await fireEvent.click(screen.getByRole("button", { name: "Hugo" }));

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Propriétaire" }));

    // Then
    expect(
      screen.getByRole("button", { name: "Propriétaire", pressed: true }),
    ).toBeDefined();
  });

  it("adds as propriétaire with the crown on", async () => {
    // Given
    const { wrapper, screen } = open();
    await fireEvent.click(screen.getByRole("button", { name: "Hugo" }));
    await fireEvent.click(screen.getByRole("button", { name: "Propriétaire" }));

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Ajouter" }));

    // Then
    expect(wrapper.emitted("add")).toEqual([[hugo.id, "OWNER"]]);
  });

  it("moves the form to another reader, the crown off again", async () => {
    // Given
    const { screen } = open();
    await fireEvent.click(screen.getByRole("button", { name: "Hugo" }));
    await fireEvent.click(screen.getByRole("button", { name: "Propriétaire" }));

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Marc" }));

    // Then
    const marcRow = rowOf(screen, "Marc");
    expect(
      within(marcRow).getByRole("button", {
        name: "Propriétaire",
        pressed: false,
      }),
    ).toBeDefined();
    expect(
      within(marcRow).getByRole("button", { name: "Ajouter" }),
    ).toBeDefined();
    expect(
      within(rowOf(screen, "Hugo")).getByRole("button", { name: "Hugo" }),
    ).toBeDefined();
  });

  it("asks to close on Fermer", async () => {
    // Given
    const { wrapper, screen } = open();

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Fermer" }));

    // Then
    expect(wrapper.emitted("close")).toHaveLength(1);
  });

  it("is busy while the member is added", async () => {
    // Given
    const { wrapper, screen } = open();
    await fireEvent.click(screen.getByRole("button", { name: "Hugo" }));

    // When
    await wrapper.setProps({ adding: true });

    // Then
    const busy = screen.getByRole<HTMLButtonElement>("button", {
      name: "Ajout en cours…",
    });
    expect(busy.disabled).toBe(true);
    expect(screen.queryByRole("button", { name: "Ajouter" })).toBeNull();
  });

  it("accepts nothing else while the member is added", async () => {
    // Given
    const { wrapper, screen } = open();
    await fireEvent.click(screen.getByRole("button", { name: "Hugo" }));

    // When
    await wrapper.setProps({ adding: true });

    // Then
    expect(
      ["Marc", "Propriétaire", "Fermer"].map(
        (name) =>
          screen.getByRole<HTMLButtonElement>("button", { name }).disabled,
      ),
    ).toEqual([true, true, true]);
  });

  it("says Libris did not answer under the chosen reader's name", async () => {
    // Given
    const { wrapper, screen } = open();
    await fireEvent.click(screen.getByRole("button", { name: "Hugo" }));

    // When
    await wrapper.setProps({ failed: true });

    // Then
    expect(
      within(formRow(screen)).getByText(
        "Erreur lors de l'ajout, veuillez réessayer plus tard.",
      ),
    ).toBeDefined();
  });

  it("moves the form to another reader without the message", async () => {
    // Given
    const { wrapper, screen } = open();
    await fireEvent.click(screen.getByRole("button", { name: "Hugo" }));
    await wrapper.setProps({ failed: true });

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Marc" }));

    // Then
    expect(
      screen.queryByText(
        "Erreur lors de l'ajout, veuillez réessayer plus tard.",
      ),
    ).toBeNull();
  });

  it("brings itself into view when it opens", () => {
    // Given
    const scrollIntoView = vi.fn();
    Object.defineProperty(HTMLElement.prototype, "scrollIntoView", {
      configurable: true,
      value: scrollIntoView,
    });

    // When
    const { wrapper } = open();

    // Then
    expect(scrollIntoView).toHaveBeenCalledExactlyOnceWith({
      block: "nearest",
    });
    expect(scrollIntoView.mock.contexts[0]).toBe(wrapper.element);
  });

  function open(adding = false) {
    const wrapper = mount(ReaderChoice, {
      props: { readers: [hugo, marc], adding },
      global: { plugins: [createLibrisI18n()] },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }

  function formRow(screen: Screen) {
    return within(screen.getByRole("list", { name: "Choisir un lecteur" }))
      .getAllByRole("listitem")
      .find((row) =>
        within(row).queryByRole("button", { name: "Propriétaire" }),
      )!;
  }

  function rowOf(screen: Screen, displayName: string) {
    return within(screen.getByRole("list", { name: "Choisir un lecteur" }))
      .getAllByRole("listitem")
      .find((row) => row.textContent?.includes(displayName))!;
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
