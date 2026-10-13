import { fireEvent, within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createLibrisI18n } from "../i18n";
import NewBookshelfRow from "./NewBookshelfRow.vue";

describe("NewBookshelfRow", () => {
  it("offers Créer once a name is typed", async () => {
    // Given
    const { screen } = open();

    // When
    await type(screen, "Salon");

    // Then
    screen.getByRole("button", { name: "Créer" });
  });

  it("offers no Créer while the name is blank", async () => {
    // Given
    const { screen } = open();

    // When
    await type(screen, "   ");

    // Then
    expect(screen.queryByRole("button", { name: "Créer" })).toBeNull();
  });

  it("asks to create the name typed", async () => {
    // Given
    const { wrapper, screen } = open();
    await type(screen, "Bibliothèque familiale");

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Créer" }));

    // Then
    expect(wrapper.emitted("create")).toEqual([["Bibliothèque familiale"]]);
  });

  function open() {
    const wrapper = mount(NewBookshelfRow, {
      props: { creating: false },
      global: { plugins: [createLibrisI18n()] },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }

  async function type(screen: ReturnType<typeof within>, name: string) {
    await fireEvent.input(
      screen.getByRole("textbox", { name: "Nom de la bibliothèque" }),
      { target: { value: name } },
    );
  }
});
