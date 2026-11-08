import { within } from "@testing-library/dom";
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
