import { Check, ChevronDown, Trash2 } from "@lucide/vue";
import { fireEvent, within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { AuthorRole, Kind } from "../../domain/SourceEdition";
import { createLibrisI18n } from "../i18n";
import AuthorBox from "./AuthorBox.vue";

describe("AuthorBox", () => {
  afterEach(() => {
    Reflect.deleteProperty(HTMLElement.prototype, "scrollIntoView");
  });

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

  it("names the trash after the name typed", async () => {
    // Given
    const box = screen("René", [], "BD");

    // When
    await fireEvent.input(
      box.getByRole("textbox", { name: "Nom de l'auteur" }),
      {
        target: { value: "René Goscinny" },
      },
    );

    // Then
    expect(
      box.getAllByRole("button", { name: "Retirer René Goscinny" }),
    ).toHaveLength(1);
  });

  it("asks to be removed on the trash's tap", async () => {
    // Given
    const wrapper = box("René Goscinny", ["WRITER"], "BD");

    // When
    await fireEvent.click(
      within(wrapper.element as HTMLElement).getByRole("button", {
        name: "Retirer René Goscinny",
      }),
    );

    // Then
    expect(wrapper.emitted("remove")).toEqual([[]]);
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

  it("opens the roles' list on its line's tap", async () => {
    // Given
    const box = screen("René Goscinny", ["WRITER"], "BD");

    // When
    await fireEvent.click(rolesLine(box)!);

    // Then
    expect(rolesLine(box)?.getAttribute("aria-expanded")).toBe("true");
    expect(rowsOf(box).map((row) => row.textContent?.trim())).toEqual([
      "scénario",
      "dessin",
      "couleurs",
      "traduction",
    ]);
  });

  it("words a book's roles as a book's in the list", async () => {
    // Given
    const box = screen("Victor Hugo", ["WRITER"], "BOOK");

    // When
    await fireEvent.click(rolesLine(box)!);

    // Then
    expect(rowsOf(box).map((row) => row.textContent?.trim())).toEqual([
      "texte",
      "illustration",
      "couleurs",
      "traduction",
    ]);
  });

  it("presses the roles chosen", async () => {
    // Given
    const wrapper = box("René Goscinny", ["WRITER", "ARTIST"], "BD");
    const view = within(wrapper.element as HTMLElement);

    // When
    await fireEvent.click(rolesLine(view)!);

    // Then
    const rows = rowsOf(view);
    expect(rows.map((row) => row.getAttribute("aria-pressed"))).toEqual([
      "true",
      "true",
      "false",
      "false",
    ]);
    const checks = wrapper.findAllComponents(Check);
    expect(checks).toHaveLength(2);
    expect(
      checks.map((check) =>
        rows.findIndex((row) => row.contains(check.element)),
      ),
    ).toEqual([0, 1]);
  });

  it("hands on the roles with a role released", async () => {
    // Given
    const wrapper = box("René Goscinny", ["WRITER", "ARTIST"], "BD");
    const view = within(wrapper.element as HTMLElement);
    await fireEvent.click(rolesLine(view)!);

    // When
    await fireEvent.click(role(view, "dessin"));

    // Then
    expect(wrapper.emitted("update:roles")).toEqual([[["WRITER"]]]);
  });

  it("hands on a role chosen in the card's order", async () => {
    // Given
    const wrapper = box("René Goscinny", ["TRANSLATOR"], "BD");
    const view = within(wrapper.element as HTMLElement);
    await fireEvent.click(rolesLine(view)!);

    // When
    await fireEvent.click(role(view, "scénario"));

    // Then
    expect(wrapper.emitted("update:roles")).toEqual([
      [["WRITER", "TRANSLATOR"]],
    ]);
  });

  it("closes its list on its line's second tap", async () => {
    // Given
    const box = screen("René Goscinny", ["WRITER"], "BD");
    await fireEvent.click(rolesLine(box)!);

    // When
    await fireEvent.click(rolesLine(box)!);

    // Then
    expectClosed(box);
  });

  it("closes its list on a tap on its name", async () => {
    // Given
    const wrapper = box("René Goscinny", ["WRITER"], "BD", document.body);
    const view = within(wrapper.element as HTMLElement);
    await fireEvent.click(rolesLine(view)!);

    // When
    await fireEvent.click(
      view.getByRole("textbox", { name: "Nom de l'auteur" }),
    );

    // Then
    expectClosed(view);
    wrapper.unmount();
  });

  it("closes its list on a tap outside the box", async () => {
    // Given
    const box = screen("René Goscinny", ["WRITER"], "BD");
    await fireEvent.click(rolesLine(box)!);

    // When
    await fireEvent.click(document.body);

    // Then
    expectClosed(box);
  });

  it("keeps its list open on a role's tap", async () => {
    // Given
    const wrapper = box("René Goscinny", ["WRITER"], "BD", document.body);
    const view = within(wrapper.element as HTMLElement);
    await fireEvent.click(rolesLine(view)!);

    // When
    await fireEvent.click(role(view, "couleurs"));

    // Then
    expect(rolesLine(view)?.getAttribute("aria-expanded")).toBe("true");
    expect(view.getAllByRole("group", { name: "Rôles" })).toHaveLength(1);
    wrapper.unmount();
  });

  it("keeps its list open on a tap on a chosen role's check", async () => {
    // Given
    const wrapper = box(
      "René Goscinny",
      ["WRITER", "ARTIST"],
      "BD",
      document.body,
    );
    const view = within(wrapper.element as HTMLElement);
    await fireEvent.click(rolesLine(view)!);
    rendersBeforeDocumentHears(wrapper);

    // When
    await fireEvent.click(wrapper.findAllComponents(Check)[0]!.element);

    // Then
    expect(wrapper.emitted("update:roles")).toEqual([[["ARTIST"]]]);
    expect(rolesLine(view)?.getAttribute("aria-expanded")).toBe("true");
    expect(view.getAllByRole("group", { name: "Rôles" })).toHaveLength(1);
    wrapper.unmount();
  });

  it("suggests under its box the house's names that contain the name typed", async () => {
    // Given
    const view = screen("", [], "BOOK", [
      "René Goscinny",
      "Albert Uderzo",
      "Hergé",
    ]);
    const name = view.getByRole("textbox", { name: "Nom de l'auteur" });
    await fireEvent.focus(name);

    // When
    await fireEvent.input(name, { target: { value: "er" } });

    // Then
    expect(suggestions(view)).toEqual(["Albert Uderzo", "Hergé"]);
  });

  it("suggests nothing while the name has not the focus", async () => {
    // Given
    const view = screen("", [], "BOOK", [
      "René Goscinny",
      "Albert Uderzo",
      "Hergé",
    ]);

    // When
    await fireEvent.input(
      view.getByRole("textbox", { name: "Nom de l'auteur" }),
      { target: { value: "er" } },
    );

    // Then
    expect(suggestions(view)).toEqual([]);
  });

  it("puts the tapped name in place of the text and closes them", async () => {
    // Given
    const wrapper = box("", [], "BOOK", undefined, [
      "René Goscinny",
      "Albert Uderzo",
      "Hergé",
    ]);
    const view = within(wrapper.element as HTMLElement);
    const name = view.getByRole<HTMLInputElement>("textbox", {
      name: "Nom de l'auteur",
    });
    await fireEvent.focus(name);
    await fireEvent.input(name, { target: { value: "rené gos" } });

    // When
    await fireEvent.click(view.getByRole("button", { name: "René Goscinny" }));

    // Then
    expect(name.value).toBe("René Goscinny");
    expect(suggestions(view)).toEqual([]);
    expect(wrapper.emitted("update:name")?.at(-1)).toEqual(["René Goscinny"]);
  });

  it("offers them again on the next keystroke", async () => {
    // Given
    const view = screen("", [], "BOOK", [
      "René Goscinny",
      "Albert Uderzo",
      "Hergé",
    ]);
    const name = view.getByRole("textbox", { name: "Nom de l'auteur" });
    await fireEvent.focus(name);
    await fireEvent.input(name, { target: { value: "rené gos" } });
    await fireEvent.click(view.getByRole("button", { name: "René Goscinny" }));

    // When
    await fireEvent.input(name, { target: { value: "er" } });

    // Then
    expect(suggestions(view)).toHaveLength(2);
  });

  it("closes them when the name is left", async () => {
    // Given
    const view = screen("", [], "BOOK", [
      "René Goscinny",
      "Albert Uderzo",
      "Hergé",
    ]);
    const name = view.getByRole("textbox", { name: "Nom de l'auteur" });
    await fireEvent.focus(name);
    await fireEvent.input(name, { target: { value: "er" } });

    // When
    await fireEvent.blur(name);

    // Then
    expect(suggestions(view)).toEqual([]);
  });

  it("offers them again when the name takes the focus anew", async () => {
    // Given
    const view = screen("", [], "BOOK", [
      "René Goscinny",
      "Albert Uderzo",
      "Hergé",
    ]);
    const name = view.getByRole("textbox", { name: "Nom de l'auteur" });
    await fireEvent.focus(name);
    await fireEvent.input(name, { target: { value: "rené gos" } });
    await fireEvent.click(view.getByRole("button", { name: "René Goscinny" }));
    await fireEvent.blur(name);

    // When
    await fireEvent.focus(name);

    // Then
    expect(suggestions(view)).toEqual(["René Goscinny"]);
  });

  it("brings its box to the top when its name takes the focus", async () => {
    // Given
    const scrollIntoView = vi.fn();
    Object.defineProperty(HTMLElement.prototype, "scrollIntoView", {
      configurable: true,
      value: scrollIntoView,
    });
    const view = screen("", [], "BOOK");
    const name = view.getByRole("textbox", { name: "Nom de l'auteur" });

    // When
    await fireEvent.focus(name);

    // Then
    expect(scrollIntoView).toHaveBeenCalledExactlyOnceWith({ block: "start" });
    const scrolled = scrollIntoView.mock.contexts[0] as HTMLElement;
    expect(scrolled.contains(name)).toBe(true);
    expect(scrolled.contains(rolesLine(view) ?? null)).toBe(true);
  });

  function rolesLine(box: ReturnType<typeof screen>) {
    return box
      .queryAllByRole("button")
      .find((button) => button.hasAttribute("aria-expanded"));
  }

  function expectClosed(box: ReturnType<typeof screen>) {
    expect(rolesLine(box)?.getAttribute("aria-expanded")).toBe("false");
    expect(box.queryByRole("group", { name: "Rôles" })).toBeNull();
  }

  function rowsOf(box: ReturnType<typeof screen>) {
    return within(box.getByRole("group", { name: "Rôles" })).getAllByRole(
      "button",
    );
  }

  function role(box: ReturnType<typeof screen>, word: string) {
    return within(box.getByRole("group", { name: "Rôles" })).getByRole(
      "button",
      { name: word },
    );
  }

  function rendersBeforeDocumentHears(wrapper: ReturnType<typeof box>) {
    document.body.addEventListener("click", () => wrapper.vm.$.update(), {
      once: true,
    });
  }

  function suggestions(box: ReturnType<typeof screen>) {
    return box
      .queryAllByRole("button")
      .filter(
        (button) =>
          !button.hasAttribute("aria-label") &&
          !button.hasAttribute("aria-pressed"),
      )
      .map((button) => button.textContent?.trim());
  }

  function screen(
    name: string,
    roles: AuthorRole[],
    kind: Kind,
    authorNames: string[] | null = null,
  ) {
    return within(
      box(name, roles, kind, undefined, authorNames).element as HTMLElement,
    );
  }

  function box(
    name: string,
    roles: AuthorRole[],
    kind: Kind,
    attachTo?: HTMLElement,
    authorNames: string[] | null = null,
  ) {
    return mount(AuthorBox, {
      props: { name, roles, kind, authorNames },
      global: { plugins: [createLibrisI18n()] },
      attachTo,
    });
  }
});
