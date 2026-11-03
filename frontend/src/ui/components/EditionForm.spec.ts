import { BookOpen } from "@lucide/vue";
import { fireEvent, within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { Copy } from "../../domain/Copy";
import type { CoverCandidate } from "../../domain/Cover";
import type { SourceEdition } from "../../domain/SourceEdition";
import { bibliothequeDeLea } from "../../fixture/Books";
import { onePiece1 } from "../../fixture/SourceEditions";
import { createLibrisI18n } from "../i18n";
import EditionForm from "./EditionForm.vue";

const asterix1: SourceEdition = {
  ...onePiece1,
  kind: "BD",
  title: "Astérix le Gaulois",
  authors: [
    { name: "René Goscinny", role: "WRITER" },
    { name: "Albert Uderzo", role: "ARTIST" },
  ],
};

const inventaire: CoverCandidate = {
  source: "inventaire.io",
  url: "https://inventaire.io/img/entities/1",
};

const openLibrary: CoverCandidate = {
  source: "Open Library",
  url: "https://covers.openlibrary.org/b/id/1-L.jpg",
};

const onLea: Copy = {
  id: "6f1d2c3b-4a59-4e6f-8b70-1c2d3e4f5a61",
  bookshelf: bibliothequeDeLea,
};

describe("EditionForm", () => {
  afterEach(() => {
    Reflect.deleteProperty(HTMLElement.prototype, "scrollIntoView");
  });

  it("shows the ISBN under its label", () => {
    // When
    const text = show();

    // Then
    expect(text).toMatch(/ISBN.*9782000000006/);
  });

  it("says no bookshelf of the reader holds a copy", () => {
    // When
    const text = show();

    // Then
    expect(text).toContain("Dans aucune de vos bibliothèques");
  });

  it("draws the stand-in in place of a cover", () => {
    // When
    const wrapper = form();

    // Then
    expect(wrapper.findAllComponents(BookOpen)).toHaveLength(2);
    expect(
      within(wrapper.element as HTMLElement).queryByRole("img"),
    ).toBeNull();
  });

  it("offers the three kinds, Livre chosen", () => {
    // When
    const kinds = screen();

    // Then
    const radios = kinds.getAllByRole<HTMLInputElement>("radio");
    const named = ["Livre", "BD", "Manga"].map((name) =>
      kinds.getByRole<HTMLInputElement>("radio", { name }),
    );
    expect(radios).toHaveLength(3);
    expect(named.map((radio) => radios.indexOf(radio))).toEqual([0, 1, 2]);
    expect(named.map((radio) => radio.checked)).toEqual([true, false, false]);
  });

  it("offers the fields from Titre to Résumé, labelled and empty", () => {
    // When
    const fields = screen();

    // Then
    const textboxes = fields.getAllByRole<HTMLInputElement>("textbox");
    const named = [
      "Titre",
      "Sous-titre",
      "Série",
      "Tome",
      "Collection",
      "Éditeur",
      "Année",
      "Langue",
      "Pages",
      "Résumé",
    ].map((name) => fields.getByRole<HTMLInputElement>("textbox", { name }));
    expect(textboxes).toHaveLength(10);
    expect(named.map((field) => textboxes.indexOf(field))).toEqual([
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
    ]);
    expect(named.map((field) => field.value)).toEqual(Array(10).fill(""));
  });

  it("shows no placeholder", () => {
    // When
    const fields = screen();

    // Then
    expect(fields.queryAllByPlaceholderText(/./)).toEqual([]);
  });

  it("takes the numeric keyboard for Tome, Année and Pages", () => {
    // When
    const fields = screen();

    // Then
    const numeric = fields
      .getAllByRole<HTMLInputElement>("textbox")
      .filter((field) => field.getAttribute("inputmode") === "numeric")
      .map((field) => field.labels?.[0]?.textContent?.trim());
    expect(numeric).toEqual(["Tome", "Année", "Pages"]);
  });

  it("keeps a number as typed", async () => {
    // Given
    const pages = screen().getByLabelText<HTMLInputElement>("Pages");

    // When
    await fireEvent.input(pages, { target: { value: "1,5" } });

    // Then
    expect(pages.value).toBe("1,5");
  });

  it("keeps the line breaks typed in Résumé", async () => {
    // Given
    const summary = screen().getByLabelText<HTMLTextAreaElement>("Résumé");

    // When
    await fireEvent.input(summary, { target: { value: "Un.\nDeux." } });

    // Then
    expect(summary.value).toBe("Un.\nDeux.");
  });

  it("holds in each field what Libris answered", () => {
    // When
    const fields = screen({ ...onePiece1, summary: "Un.\nDeux." });

    // Then
    expect(valuesOf(fields)).toEqual([
      "Romance dawn",
      "à l'aube d'une grande aventure",
      "One piece",
      "1",
      "Shonen manga",
      "Glénat",
      "2013",
      "français",
      "203",
      "Un.\nDeux.",
    ]);
  });

  it("leaves empty the fields Libris answered nothing for", () => {
    // When
    const fields = screen({
      ...onePiece1,
      subtitle: null,
      series: null,
      collection: null,
      publisher: null,
      publicationYear: null,
      language: null,
      pageCount: null,
      summary: null,
    });

    // Then
    expect(valuesOf(fields)).toEqual(["Romance dawn", ...Array(9).fill("")]);
  });

  it("chooses the kind Libris answered", () => {
    // When
    const kinds = screen(onePiece1);

    // Then
    expect(
      kinds
        .getAllByRole<HTMLInputElement>("radio")
        .filter((radio) => radio.checked)
        .map((radio) => radio.labels?.[0]?.textContent?.trim()),
    ).toEqual(["Manga"]);
  });

  it("names the volume field Album for a BD", async () => {
    // Given
    const fields = screen();

    // When
    await fireEvent.change(fields.getByRole("radio", { name: "BD" }), {
      target: { checked: true },
    });

    // Then
    expect(fields.getByRole("textbox", { name: "Album" })).toBeDefined();
    expect(fields.queryByRole("textbox", { name: "Tome" })).toBeNull();
  });

  it("names it Tome again for a Manga", async () => {
    // Given
    const fields = screen();
    await fireEvent.change(fields.getByRole("radio", { name: "BD" }), {
      target: { checked: true },
    });

    // When
    await fireEvent.change(fields.getByRole("radio", { name: "Manga" }), {
      target: { checked: true },
    });

    // Then
    expect(fields.getByRole("textbox", { name: "Tome" })).toBeDefined();
  });

  it("shows the authors as the card words them", () => {
    // When
    const text = show(asterix1);

    // Then
    const positions = [
      "Album",
      "Auteurs",
      "René Goscinny · scénario",
      "Albert Uderzo · dessin",
      "Collection",
    ].map((part) => text.indexOf(part));
    expect(positions).not.toContain(-1);
    expect(positions).toEqual([...positions].sort((a, b) => a - b));
  });

  it("words the authors' roles in the chosen kind", async () => {
    // Given
    const wrapper = form({ ...asterix1, kind: "BOOK" });
    const fields = within(wrapper.element as HTMLElement);

    // When
    await fireEvent.change(fields.getByRole("radio", { name: "BD" }), {
      target: { checked: true },
    });

    // Then
    const text = wrapper.text().replace(/\s+/g, " ");
    expect(text).toContain("René Goscinny · scénario");
    expect(text).toContain("Albert Uderzo · dessin");
    expect(text).not.toContain("texte");
  });

  it("shows no Auteurs when Libris answered none", () => {
    // When
    const text = show({ ...onePiece1, authors: [] });

    // Then
    expect(text).not.toContain("Auteurs");
  });

  it("shows the covers on offer as the card does", () => {
    // When
    const covers = screen(onePiece1, [], [inventaire, openLibrary]);

    // Then
    expect(
      covers
        .getByRole("img", { name: "Couverture de Romance dawn" })
        .getAttribute("src"),
    ).toBe(inventaire.url);
    expect(
      covers
        .getAllByRole("button", { name: /^Couverture / })
        .map((dot) => dot.getAttribute("aria-label")),
    ).toEqual(["Couverture inventaire.io", "Couverture Open Library"]);
    expect(covers.getByText("inventaire.io")).toBeDefined();
  });

  it("shows first the cover the screen names", () => {
    // When
    const covers = within(
      form(onePiece1, [], [inventaire, openLibrary], {
        shownSource: "Open Library",
      }).element as HTMLElement,
    );

    // Then
    expect(
      covers
        .getByRole("img", { name: "Couverture de Romance dawn" })
        .getAttribute("src"),
    ).toBe(openLibrary.url);
    expect(
      covers
        .getAllByRole("button", { name: /^Couverture / })
        .map((dot) => dot.getAttribute("aria-pressed")),
    ).toEqual(["false", "true"]);
    expect(covers.getByText("Open Library")).toBeDefined();
  });

  it("gives out the source of the cover it shows", async () => {
    // Given
    const wrapper = form(onePiece1, [], [inventaire, openLibrary]);

    // When
    await fireEvent.click(
      within(wrapper.element as HTMLElement).getByRole("button", {
        name: "Couverture Open Library",
      }),
    );

    // Then
    expect(wrapper.emitted("coverSource")).toEqual([
      ["inventaire.io"],
      ["Open Library"],
    ]);
  });

  it("draws the button the screen gives in its corner", () => {
    // When
    const wrapper = form(onePiece1, [], [], {
      corner: '<button type="button">Agir</button>',
    });

    // Then
    expect(
      within(wrapper.element as HTMLElement).getByRole("button", {
        name: "Agir",
      }),
    ).toBeDefined();
  });

  it("says in which bookshelf the reader has a copy", () => {
    // When
    const text = show(onePiece1, [onLea]);

    // Then
    expect(text).toContain("Dans Bibliothèque de Léa");
    expect(text).not.toContain("Dans aucune de vos bibliothèques");
  });

  it("sends the fields as the reader left them", async () => {
    // Given
    const wrapper = form(onePiece1);
    const screen = within(wrapper.element as HTMLElement);
    await fireEvent.input(screen.getByRole("textbox", { name: "Titre" }), {
      target: { value: "Romance Dawn" },
    });

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "Ajouter à ma bibliothèque" }),
    );

    // Then
    expect(wrapper.emitted("add")).toEqual([
      [{ ...onePiece1, title: "Romance Dawn" }],
    ]);
  });

  it("sends the kind chosen", async () => {
    // Given
    const wrapper = form(onePiece1);
    const screen = within(wrapper.element as HTMLElement);
    await fireEvent.change(screen.getByRole("radio", { name: "BD" }), {
      target: { checked: true },
    });

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "Ajouter à ma bibliothèque" }),
    );

    // Then
    expect(wrapper.emitted("add")?.[0]?.[0]).toMatchObject({ kind: "BD" });
  });

  it("sends an unknown ISBN's digits and no author", async () => {
    // Given
    const wrapper = form();
    const screen = within(wrapper.element as HTMLElement);
    await fireEvent.input(screen.getByRole("textbox", { name: "Titre" }), {
      target: { value: "Astérix" },
    });

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "Ajouter à ma bibliothèque" }),
    );

    // Then
    expect(wrapper.emitted("add")).toEqual([
      [
        {
          isbn13: "9782000000006",
          kind: "BOOK",
          title: "Astérix",
          subtitle: null,
          authors: [],
          series: null,
          collection: null,
          publisher: null,
          publicationYear: null,
          language: null,
          pageCount: null,
          summary: null,
        },
      ],
    ]);
  });

  it("accepts nothing while the add runs", () => {
    // When
    const fields = within(
      form(onePiece1, [], [inventaire, openLibrary], { adding: true })
        .element as HTMLElement,
    );

    // Then
    expect(disabledOf(fields)).toEqual(Array(15).fill(true));
  });

  it("takes edits again once the add failed", () => {
    // When
    const wrapper = form(onePiece1, [], [inventaire, openLibrary], {
      failed: true,
    });

    // Then
    expect(wrapper.text()).toContain(
      "Erreur lors de l'ajout, veuillez réessayer plus tard.",
    );
    expect(disabledOf(within(wrapper.element as HTMLElement))).toEqual(
      Array(15).fill(false),
    );
  });

  it("sends nothing while the title is missing", async () => {
    // Given
    const wrapper = form(onePiece1);
    const screen = within(wrapper.element as HTMLElement);
    await typeIn(screen, "Titre", "");

    // When
    await tapAdd(screen);

    // Then
    expect(wrapper.emitted("add")).toBeUndefined();
    expect(wrapper.text()).toContain("Titre manquant");
    expect(isRefused(screen, "Titre")).toBe(true);
  });

  it("says Nombre invalide under the refused number", async () => {
    // Given
    const wrapper = form(onePiece1);
    const screen = within(wrapper.element as HTMLElement);
    await typeIn(screen, "Année", "deux mille");

    // When
    await tapAdd(screen);

    // Then
    expect(wrapper.text()).toMatch(/Année.*Nombre invalide.*Langue/s);
    expect(isRefused(screen, "Année")).toBe(true);
  });

  it("says Tome sans série under a volume number with no series", async () => {
    // Given
    const wrapper = form({ ...onePiece1, kind: "BOOK" });
    const screen = within(wrapper.element as HTMLElement);
    await typeIn(screen, "Série", "");

    // When
    await tapAdd(screen);

    // Then
    expect(wrapper.emitted("add")).toBeUndefined();
    expect(wrapper.text()).toMatch(/Tome.*Tome sans série.*Auteurs/s);
    expect(isRefused(screen, "Tome")).toBe(true);
  });

  it("says Album sans série for a BD", async () => {
    // Given
    const wrapper = form(onePiece1);
    const screen = within(wrapper.element as HTMLElement);
    await fireEvent.change(screen.getByRole("radio", { name: "BD" }), {
      target: { checked: true },
    });
    await typeIn(screen, "Série", "");

    // When
    await tapAdd(screen);

    // Then
    expect(wrapper.text()).toContain("Album sans série");
    expect(isRefused(screen, "Album")).toBe(true);
  });

  it("rewords a refusal shown when the kind changes", async () => {
    // Given
    const wrapper = form(onePiece1);
    const screen = within(wrapper.element as HTMLElement);
    await typeIn(screen, "Série", "");
    await tapAdd(screen);

    // When
    await fireEvent.change(screen.getByRole("radio", { name: "BD" }), {
      target: { checked: true },
    });

    // Then
    expect(wrapper.text()).toContain("Album sans série");
    expect(wrapper.text()).not.toContain("Tome sans série");
    expect(wrapper.emitted("add")).toBeUndefined();
  });

  it("says Langue inconnue under a word naming no language", async () => {
    // Given
    const wrapper = form(onePiece1);
    const screen = within(wrapper.element as HTMLElement);
    await typeIn(screen, "Langue", "elfique");

    // When
    await tapAdd(screen);

    // Then
    expect(wrapper.text()).toContain("Langue inconnue");
    expect(isRefused(screen, "Langue")).toBe(true);
  });

  it("checks nothing before the tap", async () => {
    // Given
    const wrapper = form(onePiece1);
    const screen = within(wrapper.element as HTMLElement);

    // When
    await typeIn(screen, "Titre", "");

    // Then
    expect(wrapper.text()).not.toContain("Titre manquant");
    expect(
      screen
        .getAllByRole("textbox")
        .filter((field) => field.getAttribute("aria-invalid") === "true"),
    ).toEqual([]);
  });

  it("focuses the first refused field", async () => {
    // Given
    const wrapper = form(onePiece1, [], [], { attachTo: document.body });
    const screen = within(wrapper.element as HTMLElement);
    await typeIn(screen, "Titre", "");
    await typeIn(screen, "Pages", "0");
    const button = screen.getByRole("button", {
      name: "Ajouter à ma bibliothèque",
    });
    button.focus();

    // When
    await fireEvent.click(button);

    // Then
    expect(document.activeElement).toBe(
      screen.getByRole("textbox", { name: "Titre" }),
    );
    wrapper.unmount();
  });

  it("brings the first refused field into view", async () => {
    // Given
    const scrollIntoView = vi.fn();
    Object.defineProperty(HTMLElement.prototype, "scrollIntoView", {
      configurable: true,
      value: scrollIntoView,
    });
    const wrapper = form(onePiece1);
    const screen = within(wrapper.element as HTMLElement);
    await typeIn(screen, "Titre", "");
    await typeIn(screen, "Pages", "0");

    // When
    await tapAdd(screen);

    // Then
    expect(scrollIntoView).toHaveBeenCalledExactlyOnceWith({
      block: "center",
    });
    expect(scrollIntoView.mock.contexts[0]).toBe(
      screen.getByRole("textbox", { name: "Titre" }),
    );
  });

  it("keeps a refusal until the next tap", async () => {
    // Given
    const wrapper = form(onePiece1);
    const screen = within(wrapper.element as HTMLElement);
    await typeIn(screen, "Titre", "");
    await tapAdd(screen);

    // When
    await typeIn(screen, "Titre", "Romance Dawn");

    // Then
    expect(wrapper.text()).toContain("Titre manquant");
    expect(isRefused(screen, "Titre")).toBe(true);
  });

  it("sends the form once the refused field is mended", async () => {
    // Given
    const wrapper = form(onePiece1);
    const screen = within(wrapper.element as HTMLElement);
    await typeIn(screen, "Titre", "");
    await tapAdd(screen);
    await typeIn(screen, "Titre", "Romance Dawn");

    // When
    await tapAdd(screen);

    // Then
    expect(wrapper.emitted("add")).toEqual([
      [{ ...onePiece1, title: "Romance Dawn" }],
    ]);
    expect(wrapper.text()).not.toContain("Titre manquant");
  });

  function disabledOf(fields: ReturnType<typeof screen>) {
    return [
      ...fields.getAllByRole<HTMLInputElement>("textbox"),
      ...fields.getAllByRole<HTMLInputElement>("radio"),
      ...fields.getAllByRole<HTMLButtonElement>("button", {
        name: /^Couverture /,
      }),
    ].map((field) => field.disabled);
  }

  async function typeIn(
    fields: ReturnType<typeof screen>,
    label: string,
    text: string,
  ) {
    await fireEvent.input(fields.getByRole("textbox", { name: label }), {
      target: { value: text },
    });
  }

  function isRefused(fields: ReturnType<typeof screen>, label: string) {
    return (
      fields
        .getByRole("textbox", { name: label })
        .getAttribute("aria-invalid") === "true"
    );
  }

  async function tapAdd(fields: ReturnType<typeof screen>) {
    await fireEvent.click(
      fields.getByRole("button", { name: "Ajouter à ma bibliothèque" }),
    );
  }

  function valuesOf(fields: ReturnType<typeof screen>) {
    return fields
      .getAllByRole<HTMLInputElement>("textbox")
      .map((field) => field.value);
  }

  function show(
    edition?: SourceEdition,
    copies: Copy[] = [],
    covers: CoverCandidate[] = [],
  ) {
    return form(edition, copies, covers).text().replace(/\s+/g, " ");
  }

  function screen(
    edition?: SourceEdition,
    copies: Copy[] = [],
    covers: CoverCandidate[] = [],
  ) {
    return within(form(edition, copies, covers).element as HTMLElement);
  }

  function form(
    edition?: SourceEdition,
    copies: Copy[] = [],
    covers: CoverCandidate[] = [],
    {
      adding = false,
      failed = false,
      corner,
      shownSource,
      attachTo,
    }: {
      adding?: boolean;
      failed?: boolean;
      corner?: string;
      shownSource?: string;
      attachTo?: HTMLElement;
    } = {},
  ) {
    return mount(EditionForm, {
      props: {
        isbn13: edition?.isbn13 ?? "9782000000006",
        edition,
        copies,
        covers,
        adding,
        failed,
        shownSource,
      },
      slots: corner === undefined ? {} : { corner },
      global: { plugins: [createLibrisI18n()] },
      attachTo,
    });
  }
});
