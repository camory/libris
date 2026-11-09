import { ChevronDown, Trash2 } from "@lucide/vue";
import { fireEvent, within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { AuthorRole, Kind } from "../../domain/SourceEdition";
import { createLibrisI18n } from "../i18n";
import AuthorBox from "./AuthorBox.vue";

describe("AuthorBox", () => {
  it("holds the author's name", () => {
    // When
    const box = screen("René Goscinny", [], "BD");

    // Then
    expect(
      box.getByRole<HTMLInputElement>("textbox", { name: "Nom de l'auteur" })
        .value,
    ).toBe("René Goscinny");
  });

  it("offers the trash, named Retirer and the name", () => {
    // When
    const wrapper = box("René Goscinny", [], "BD");

    // Then
    const trashes = within(wrapper.element as HTMLElement).getAllByRole(
      "button",
      { name: "Retirer René Goscinny" },
    );
    expect(trashes).toHaveLength(1);
    expect(trashes[0]?.contains(wrapper.findComponent(Trash2).element)).toBe(
      true,
    );
  });

  it("names the trash Retirer l'auteur while the name is empty", () => {
    // When
    const box = screen("", [], "BD");

    // Then
    const trash = box.getByRole("button", { name: /^Retirer/ });
    expect(trash.getAttribute("aria-label")).toBe("Retirer l'auteur");
  });

  it("reads the roles in the words of the kind", () => {
    // When
    const box = screen("René Goscinny", ["WRITER", "ARTIST"], "BD");

    // Then
    expect(rolesLine(box)?.textContent?.trim()).toBe("scénario, dessin");
  });

  it("words the roles of a book as a book's", () => {
    // When
    const box = screen("Victor Hugo", ["WRITER"], "BOOK");

    // Then
    expect(rolesLine(box)?.textContent?.trim()).toBe("texte");
  });

  it("names the roles line after the roles it reads", () => {
    // When
    const box = screen("René Goscinny", ["WRITER", "ARTIST"], "BD");

    // Then
    expect(
      box.getAllByRole("button", { name: "Rôles scénario, dessin" }),
    ).toHaveLength(1);
  });

  it("names the roles line Rôles alone when it reads none", () => {
    // When
    const box = screen("René Goscinny", [], "BD");

    // Then
    const line = box.getByRole("button", {
      name: (name) => name === "Rôles",
    });
    expect(line.getAttribute("aria-label")).toBe("Rôles");
    expect(line.textContent?.trim()).toBe("");
  });

  it("keeps the roles' list closed", () => {
    // When
    const wrapper = box("René Goscinny", ["WRITER"], "BD");

    // Then
    const line = rolesLine(within(wrapper.element as HTMLElement));
    expect(line?.getAttribute("aria-expanded")).toBe("false");
    expect(line?.contains(wrapper.findComponent(ChevronDown).element)).toBe(
      true,
    );
  });

  it("hands on the name typed", async () => {
    // Given
    const wrapper = box("", [], "BOOK");

    // When
    await fireEvent.input(
      within(wrapper.element as HTMLElement).getByRole("textbox", {
        name: "Nom de l'auteur",
      }),
      { target: { value: "René" } },
    );

    // Then
    expect(wrapper.emitted("update:name")).toEqual([["René"]]);
  });

  function rolesLine(box: ReturnType<typeof screen>) {
    return box
      .queryAllByRole("button")
      .find((button) => button.hasAttribute("aria-expanded"));
  }

  function screen(name: string, roles: AuthorRole[], kind: Kind) {
    return within(box(name, roles, kind).element as HTMLElement);
  }

  function box(name: string, roles: AuthorRole[], kind: Kind) {
    return mount(AuthorBox, {
      props: { name, roles, kind },
      global: { plugins: [createLibrisI18n()] },
    });
  }
});
