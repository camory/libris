import { BookOpen, Check } from "@lucide/vue";
import { fireEvent, within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
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

const houseSeries = [
  "Pierrot",
  "Spirou et Fantasio",
  "Piège",
  "One piece",
  "Les Pieds Nickelés",
  "Astérix",
  "Épiés",
  "Pièces détachées",
];

const onLea: Copy = {
  id: "6f1d2c3b-4a59-4e6f-8b70-1c2d3e4f5a61",
  bookshelf: { ...bibliothequeDeLea, role: "OWNER" },
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

  it("opens one box per author in the card's order", () => {
    // When
    const fields = screen({
      ...asterix1,
      authors: [
        { name: "Albert Uderzo", role: "ARTIST" },
        { name: "René Goscinny", role: "WRITER" },
      ],
    });

    // Then
    expect(authorsOf(fields)).toEqual([
      { name: "René Goscinny", roles: "scénario" },
      { name: "Albert Uderzo", roles: "dessin" },
    ]);
    const textboxes = fields.getAllByRole("textbox");
    expect(
      [
        fields.getByRole("textbox", { name: "Album" }),
        ...names(fields),
        fields.getByRole("textbox", { name: "Collection" }),
      ].map((box) => textboxes.indexOf(box)),
    ).toEqual([3, 4, 5, 6]);
  });

  it("keeps an author of several roles in one box", () => {
    // When
    const fields = screen(onePiece1);

    // Then
    expect(authorsOf(fields)).toEqual([
      { name: "Eiichirō Oda", roles: "scénario, dessin" },
    ]);
  });

  it("words the authors' roles in the chosen kind", async () => {
    // Given
    const fields = screen(asterix1);

    // When
    await fireEvent.change(fields.getByRole("radio", { name: "Livre" }), {
      target: { checked: true },
    });

    // Then
    expect(rolesLines(fields).map((line) => line.textContent?.trim())).toEqual([
      "texte",
      "illustration",
    ]);
  });

  it("keeps a typed name when the kind changes", async () => {
    // Given
    const fields = screen(asterix1);
    await fireEvent.input(names(fields)[0], { target: { value: "René" } });

    // When
    await fireEvent.change(fields.getByRole("radio", { name: "Livre" }), {
      target: { checked: true },
    });

    // Then
    expect(names(fields)[0].value).toBe("René");
  });

  it("keeps the roles chosen when the kind changes", async () => {
    // Given
    const fields = screen(asterix1);
    await fireEvent.click(rolesLines(fields)[1]!);
    await fireEvent.click(role(fields, "couleurs"));

    // When
    await fireEvent.change(fields.getByRole("radio", { name: "Livre" }), {
      target: { checked: true },
    });

    // Then
    expect(authorsOf(fields)[1]).toEqual({
      name: "Albert Uderzo",
      roles: "illustration, couleurs",
    });
  });

  it("shows Auteurs with no author", () => {
    // When
    const wrapper = form();

    // Then
    const text = wrapper.text().replace(/\s+/g, " ");
    const positions = ["Tome", "Auteurs", "Collection"].map((part) =>
      text.indexOf(part),
    );
    expect(positions).not.toContain(-1);
    expect(positions).toEqual([...positions].sort((a, b) => a - b));
    expect(
      within(wrapper.element as HTMLElement).queryAllByRole("textbox", {
        name: "Nom de l'auteur",
      }),
    ).toEqual([]);
  });

  it("offers Ajouter un auteur with no author", () => {
    // When
    const wrapper = form();

    // Then
    expect(
      within(wrapper.element as HTMLElement).getAllByRole("button", {
        name: "Ajouter un auteur",
      }),
    ).toHaveLength(1);
    const text = wrapper.text().replace(/\s+/g, " ");
    const positions = ["Auteurs", "Ajouter un auteur", "Collection"].map(
      (part) => text.indexOf(part),
    );
    expect(positions).not.toContain(-1);
    expect(positions).toEqual([...positions].sort((a, b) => a - b));
  });

  it("adds an empty box after the last, on the kind's first role", async () => {
    // Given
    const fields = screen(asterix1);

    // When
    await fireEvent.click(
      fields.getByRole("button", { name: "Ajouter un auteur" }),
    );

    // Then
    expect(authorsOf(fields)).toEqual([
      { name: "René Goscinny", roles: "scénario" },
      { name: "Albert Uderzo", roles: "dessin" },
      { name: "", roles: "scénario" },
    ]);
  });

  it("focuses the new box's name", async () => {
    // Given
    const wrapper = form(asterix1, [], [], { attachTo: document.body });
    const fields = within(wrapper.element as HTMLElement);

    // When
    await fireEvent.click(
      fields.getByRole("button", { name: "Ajouter un auteur" }),
    );
    await flushPromises();

    // Then
    expect(document.activeElement).toBe(names(fields)[2]);
    wrapper.unmount();
  });

  it("sends a name typed in its box", async () => {
    // Given
    const wrapper = form(onePiece1);
    const fields = within(wrapper.element as HTMLElement);
    await fireEvent.input(names(fields)[0], {
      target: { value: "Eiichiro Oda" },
    });

    // When
    await tapAdd(fields);

    // Then
    expect(wrapper.emitted("add")?.[0]?.[0]).toMatchObject({
      authors: [
        { name: "Eiichiro Oda", role: "WRITER" },
        { name: "Eiichiro Oda", role: "ARTIST" },
      ],
    });
  });

  it("sends the roles chosen in a box", async () => {
    // Given
    const wrapper = form(onePiece1);
    const fields = within(wrapper.element as HTMLElement);
    await fireEvent.click(rolesLines(fields)[0]!);
    await fireEvent.click(role(fields, "dessin"));
    await fireEvent.click(role(fields, "traduction"));

    // When
    await tapAdd(fields);

    // Then
    const name = onePiece1.authors[0]?.name;
    expect(wrapper.emitted("add")?.[0]?.[0]).toMatchObject({
      authors: [
        { name, role: "WRITER" },
        { name, role: "TRANSLATOR" },
      ],
    });
  });

  it("keeps the focus in a name being typed", async () => {
    // Given
    const wrapper = form(asterix1, [], [], { attachTo: document.body });
    const fields = within(wrapper.element as HTMLElement);
    names(fields)[0].focus();

    // When
    await fireEvent.input(names(fields)[0], { target: { value: "René Gosc" } });

    // Then
    expect(document.activeElement).toBe(names(fields)[0]);
    wrapper.unmount();
  });

  it("removes the box of the trash tapped", async () => {
    // Given
    const fields = screen(asterix1);

    // When
    await fireEvent.click(
      fields.getByRole("button", { name: "Retirer René Goscinny" }),
    );

    // Then
    expect(authorsOf(fields)).toEqual([
      { name: "Albert Uderzo", roles: "dessin" },
    ]);
  });

  it("focuses Ajouter un auteur once a box is removed", async () => {
    // Given
    const wrapper = form(asterix1, [], [], { attachTo: document.body });
    const fields = within(wrapper.element as HTMLElement);

    // When
    await fireEvent.click(
      fields.getByRole("button", { name: "Retirer Albert Uderzo" }),
    );
    await flushPromises();

    // Then
    expect(document.activeElement).toBe(
      fields.getByRole("button", { name: "Ajouter un auteur" }),
    );
    wrapper.unmount();
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
    expect(disabledOf(fields)).toEqual(Array(19).fill(true));
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
      Array(19).fill(false),
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

  it("sends nothing while an author has no role", async () => {
    // Given
    const wrapper = form(asterix1);
    const screen = within(wrapper.element as HTMLElement);
    await release(screen, 0, "scénario");

    // When
    await tapAdd(screen);

    // Then
    expect(wrapper.emitted("add")).toBeUndefined();
    expect(wrapper.text()).toMatch(/Auteurs.*Rôle manquant.*dessin/s);
  });

  it("focuses the roles' line of the first author refused", async () => {
    // Given
    const wrapper = form(asterix1, [], [], { attachTo: document.body });
    const screen = within(wrapper.element as HTMLElement);
    await release(screen, 0, "scénario");
    await release(screen, 1, "dessin");
    const button = screen.getByRole("button", {
      name: "Ajouter à ma bibliothèque",
    });
    button.focus();

    // When
    await fireEvent.click(button);

    // Then
    expect(document.activeElement).toBe(rolesLines(screen)[0]);
    wrapper.unmount();
  });

  it("suggests under Série the names that contain the text", async () => {
    // Given
    const fields = screen(onePiece1, [], [], { seriesNames: houseSeries });
    await fireEvent.focus(fields.getByRole("textbox", { name: "Série" }));

    // When
    await typeIn(fields, "Série", "pie");

    // Then
    expect(suggestions(fields)).toEqual([
      "Épiés",
      "Les Pieds Nickelés",
      "One piece",
      "Pièces détachées",
      "Piège",
    ]);
  });

  it("suggests nothing while Série has not the focus", async () => {
    // Given
    const fields = screen(onePiece1, [], [], { seriesNames: houseSeries });

    // When
    await typeIn(fields, "Série", "pie");

    // Then
    expect(suggestions(fields)).toEqual([]);
  });

  it("fills Série with the tapped name and closes them", async () => {
    // Given
    const wrapper = form(onePiece1, [], [], {
      seriesNames: houseSeries,
      attachTo: document.body,
    });
    const fields = within(wrapper.element as HTMLElement);
    const submitted = vi.fn((event: Event) => event.preventDefault());
    wrapper.element.addEventListener("submit", submitted);
    await fireEvent.focus(fields.getByRole("textbox", { name: "Série" }));
    await typeIn(fields, "Série", "one pi");

    // When
    await fireEvent.click(fields.getByRole("button", { name: "One piece" }));

    // Then
    expect(
      fields.getByRole<HTMLInputElement>("textbox", { name: "Série" }).value,
    ).toBe("One piece");
    expect(suggestions(fields)).toEqual([]);
    expect(submitted).not.toHaveBeenCalled();
    wrapper.unmount();
  });

  it("offers them again on the next keystroke", async () => {
    // Given
    const fields = screen(onePiece1, [], [], { seriesNames: houseSeries });
    await fireEvent.focus(fields.getByRole("textbox", { name: "Série" }));
    await typeIn(fields, "Série", "one pi");
    await fireEvent.click(fields.getByRole("button", { name: "One piece" }));

    // When
    await typeIn(fields, "Série", "pi");

    // Then
    expect(suggestions(fields)).toHaveLength(5);
  });

  it("closes them when Série is left", async () => {
    // Given
    const fields = screen(onePiece1, [], [], { seriesNames: houseSeries });
    const series = fields.getByRole("textbox", { name: "Série" });
    await fireEvent.focus(series);
    await typeIn(fields, "Série", "pie");

    // When
    await fireEvent.blur(series);

    // Then
    expect(suggestions(fields)).toEqual([]);
  });

  it("offers them again when Série takes the focus anew", async () => {
    // Given
    const fields = screen(onePiece1, [], [], { seriesNames: houseSeries });
    const series = fields.getByRole("textbox", { name: "Série" });
    await fireEvent.focus(series);
    await typeIn(fields, "Série", "one pi");
    await fireEvent.click(fields.getByRole("button", { name: "One piece" }));
    await fireEvent.blur(series);

    // When
    await fireEvent.focus(series);

    // Then
    expect(suggestions(fields)).toEqual(["One piece"]);
  });

  it("keeps the focus in Série while a row is pressed", async () => {
    // Given
    const fields = screen(onePiece1, [], [], { seriesNames: houseSeries });
    await fireEvent.focus(fields.getByRole("textbox", { name: "Série" }));
    await typeIn(fields, "Série", "one pi");

    // When
    const moved = fireEvent.mouseDown(
      fields.getByRole("button", { name: "One piece" }),
    );

    // Then
    expect(moved).toBe(false);
  });

  it("brings Série to the top when it takes the focus", async () => {
    // Given
    const scrollIntoView = vi.fn();
    Object.defineProperty(HTMLElement.prototype, "scrollIntoView", {
      configurable: true,
      value: scrollIntoView,
    });
    const fields = screen(onePiece1, [], [], { seriesNames: houseSeries });
    const series = fields.getByRole("textbox", { name: "Série" });

    // When
    await fireEvent.focus(series);

    // Then
    expect(scrollIntoView).toHaveBeenCalledExactlyOnceWith({ block: "start" });
    expect(scrollIntoView.mock.contexts[0]).toBe(series);
  });

  it("suggests under an author's box the house's names", async () => {
    // Given
    const fields = screen(onePiece1, [], [], {
      authorNames: ["René Goscinny", "Albert Uderzo", "Hergé"],
    });
    const name = fields.getByRole("textbox", { name: "Nom de l'auteur" });
    await fireEvent.focus(name);

    // When
    await fireEvent.input(name, { target: { value: "er" } });

    // Then
    expect(suggestions(fields)).toEqual(["Albert Uderzo", "Hergé"]);
  });

  it("says Existante in Série for a series of the house", () => {
    // When
    const wrapper = form(onePiece1, [], [], { seriesNames: houseSeries });

    // Then
    const fields = within(wrapper.element as HTMLElement);
    expect(fields.getByText("Existante")).toBeTruthy();
    expect(wrapper.findAllComponents(Check)).toHaveLength(1);
    expect(fields.queryByText("Nouvelle")).toBeNull();
  });

  it("says Nouvelle for a name the house does not hold", async () => {
    // Given
    const wrapper = form(onePiece1, [], [], { seriesNames: houseSeries });
    const fields = within(wrapper.element as HTMLElement);

    // When
    await typeIn(fields, "Série", "One pie");

    // Then
    expect(fields.getByText("Nouvelle")).toBeTruthy();
    expect(fields.queryByText("Existante")).toBeNull();
    expect(wrapper.findAllComponents(Check)).toHaveLength(0);
  });

  it("says no word while the names are unknown", () => {
    // When
    const fields = screen(onePiece1, [], [], { seriesNames: null });

    // Then
    expect(fields.queryByText("Existante")).toBeNull();
    expect(fields.queryByText("Nouvelle")).toBeNull();
  });

  it("suggests under Langue the words that contain the text", async () => {
    // Given
    const fields = screen();
    await fireEvent.focus(fields.getByRole("textbox", { name: "Langue" }));

    // When
    await typeIn(fields, "Langue", "AN");

    // Then
    expect(suggestions(fields)).toEqual([
      "afrikaans",
      "akan",
      "albanais",
      "allemand",
      "anglais",
    ]);
  });

  it("fills Langue with the tapped word and closes them", async () => {
    // Given
    const fields = screen();
    await fireEvent.focus(fields.getByRole("textbox", { name: "Langue" }));
    await typeIn(fields, "Langue", "jap");

    // When
    await fireEvent.click(fields.getByRole("button", { name: "japonais" }));

    // Then
    expect(
      fields.getByRole<HTMLInputElement>("textbox", { name: "Langue" }).value,
    ).toBe("japonais");
    expect(suggestions(fields)).toEqual([]);
  });

  it("closes them when Langue is left", async () => {
    // Given
    const fields = screen();
    const language = fields.getByRole("textbox", { name: "Langue" });
    await fireEvent.focus(language);
    await typeIn(fields, "Langue", "jap");

    // When
    await fireEvent.blur(language);

    // Then
    expect(suggestions(fields)).toEqual([]);
  });

  it("says no word in Langue", async () => {
    // Given
    const fields = screen(onePiece1, [], [], { seriesNames: houseSeries });
    await typeIn(fields, "Série", "");

    // When
    await typeIn(fields, "Langue", "japonais");

    // Then
    expect(fields.queryByText("Existante")).toBeNull();
    expect(fields.queryByText("Nouvelle")).toBeNull();
  });

  it("does not scroll Langue on focus", async () => {
    // Given
    const scrollIntoView = vi.fn();
    Object.defineProperty(HTMLElement.prototype, "scrollIntoView", {
      configurable: true,
      value: scrollIntoView,
    });
    const fields = screen(onePiece1, [], [], { seriesNames: houseSeries });

    // When
    await fireEvent.focus(fields.getByRole("textbox", { name: "Langue" }));

    // Then
    expect(scrollIntoView).not.toHaveBeenCalled();
  });

  function names(fields: ReturnType<typeof screen>) {
    return fields.queryAllByRole<HTMLInputElement>("textbox", {
      name: "Nom de l'auteur",
    });
  }

  function rolesLines(fields: ReturnType<typeof screen>) {
    return fields
      .queryAllByRole("button")
      .filter((button) => button.hasAttribute("aria-expanded"));
  }

  function role(fields: ReturnType<typeof screen>, word: string) {
    return within(fields.getByRole("group", { name: "Rôles" })).getByRole(
      "button",
      { name: word },
    );
  }

  async function release(
    fields: ReturnType<typeof screen>,
    box: number,
    word: string,
  ) {
    await fireEvent.click(rolesLines(fields)[box]!);
    await fireEvent.click(role(fields, word));
    await fireEvent.click(rolesLines(fields)[box]!);
  }

  function trashes(fields: ReturnType<typeof screen>) {
    return fields.queryAllByRole<HTMLButtonElement>("button", {
      name: /^Retirer\b/,
    });
  }

  function authorsOf(fields: ReturnType<typeof screen>) {
    const lines = rolesLines(fields);
    return names(fields).map((box, index) => ({
      name: box.value,
      roles: lines[index]?.textContent?.trim(),
    }));
  }

  function suggestions(fields: ReturnType<typeof screen>) {
    return fields
      .queryAllByRole<HTMLButtonElement>("button")
      .filter((button) => !button.hasAttribute("aria-expanded"))
      .filter((button) => !trashes(fields).includes(button))
      .map((button) => button.textContent?.trim() ?? "")
      .filter(
        (name) =>
          name !== "Ajouter à ma bibliothèque" && name !== "Ajouter un auteur",
      );
  }

  function disabledOf(fields: ReturnType<typeof screen>) {
    return [
      ...fieldBoxes(fields),
      ...fields.getAllByRole<HTMLInputElement>("radio"),
      ...fields.getAllByRole<HTMLButtonElement>("button", {
        name: /^Couverture /,
      }),
      ...names(fields),
      ...trashes(fields),
      ...(rolesLines(fields) as HTMLButtonElement[]),
      fields.getByRole<HTMLButtonElement>("button", {
        name: "Ajouter un auteur",
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
    return fieldBoxes(fields).map((field) => field.value);
  }

  function fieldBoxes(fields: ReturnType<typeof screen>) {
    const authors = names(fields);
    return fields
      .getAllByRole<HTMLInputElement>("textbox")
      .filter((box) => !authors.includes(box));
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
    options: Parameters<typeof form>[3] = {},
  ) {
    return within(
      form(edition, copies, covers, options).element as HTMLElement,
    );
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
      seriesNames = null,
      authorNames = null,
    }: {
      adding?: boolean;
      failed?: boolean;
      corner?: string;
      shownSource?: string;
      attachTo?: HTMLElement;
      seriesNames?: string[] | null;
      authorNames?: string[] | null;
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
        seriesNames,
        authorNames,
      },
      slots: corner === undefined ? {} : { corner },
      global: { plugins: [createLibrisI18n()] },
      attachTo,
    });
  }
});
