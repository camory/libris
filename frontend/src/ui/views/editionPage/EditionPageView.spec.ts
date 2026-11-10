import { BookOpen, CircleAlert } from "@lucide/vue";
import { fireEvent, waitFor, within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, describe, expect, it } from "vitest";
import type { Router } from "vue-router";
import {
  catalogueApiKey,
  type BookPage,
  type CatalogueApi,
  type FindBookAnswer,
} from "../../../application/CatalogueApi";
import {
  authorsApiKey,
  type AuthorsApi,
} from "../../../application/AuthorsApi";
import {
  copiesApiKey,
  type CopiesApi,
  type RemoveAnswer,
} from "../../../application/CopiesApi";
import {
  editionsApiKey,
  type EditionsApi,
  type SaveAnswer,
} from "../../../application/EditionsApi";
import { seriesApiKey, type SeriesApi } from "../../../application/SeriesApi";
import type { Book } from "../../../domain/Book";
import {
  asterixEtSesAmis,
  asterixLeGaulois,
  bibliothequeDeLea,
  romanceDawn,
  salon,
} from "../../../fixture/Books";
import { FakeAuthorsApi } from "../../../fixture/FakeAuthorsApi";
import { FakeCatalogueApi } from "../../../fixture/FakeCatalogueApi";
import { FakeCopiesApi } from "../../../fixture/FakeCopiesApi";
import { FakeEditionsApi } from "../../../fixture/FakeEditionsApi";
import { FakeSeriesApi } from "../../../fixture/FakeSeriesApi";
import { normalised } from "../../../fixture/normalised";
import HeldEditionCardSkeleton from "../../components/HeldEditionCardSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";
import { createLibrisI18n } from "../../i18n";
import { createLibrisRouter } from "../../router";
import EditionPageView from "./EditionPageView.vue";

const mountEditionPageView = (
  found: FindBookAnswer | CatalogueApi,
  id = asterixLeGaulois.id,
  {
    copiesApi = new FakeCopiesApi(new Error("no removal here")),
    router = createLibrisRouter(),
    seriesApi = new FakeSeriesApi([]),
    authorsApi = new FakeAuthorsApi([]),
    editionsApi = new FakeEditionsApi(new Error("no save here")),
  }: {
    copiesApi?: CopiesApi;
    router?: Router;
    seriesApi?: SeriesApi;
    authorsApi?: AuthorsApi;
    editionsApi?: EditionsApi;
  } = {},
) =>
  mount(EditionPageView, {
    props: { id },
    global: {
      plugins: [createLibrisI18n(), router],
      provide: {
        [catalogueApiKey]:
          "outcome" in found ? new FakeCatalogueApi([], found) : found,
        [copiesApiKey]: copiesApi,
        [seriesApiKey]: seriesApi,
        [authorsApiKey]: authorsApi,
        [editionsApiKey]: editionsApi,
      },
    },
  });

class LibrisNotRemoving implements CopiesApi {
  remove(): Promise<RemoveAnswer> {
    return new Promise(() => {});
  }
}

class LibrisNotSaving implements EditionsApi {
  save(): Promise<SaveAnswer> {
    return new Promise(() => {});
  }
}

class LibrisAnsweringOnce implements CatalogueApi {
  private answered = false;

  constructor(private readonly found: FindBookAnswer) {}

  browse(): Promise<BookPage> {
    return Promise.resolve({ books: [], next: null });
  }

  find(): Promise<FindBookAnswer> {
    if (this.answered) return new Promise(() => {});
    this.answered = true;
    return Promise.resolve(this.found);
  }
}

class LibrisRecordingAsks implements CatalogueApi {
  readonly asked: string[] = [];

  constructor(private readonly found: FindBookAnswer = noSuchOuvrage) {}

  browse(): Promise<BookPage> {
    return Promise.resolve({ books: [], next: null });
  }

  find(id: string): Promise<FindBookAnswer> {
    this.asked.push(id);
    return Promise.resolve(this.found);
  }
}

describe("EditionPageView", () => {
  afterEach(() => {
    window.history.replaceState(null, "", "/");
  });

  it("shows the card's skeleton while the ouvrage comes, no title", async () => {
    // Given
    const wrapper = mountEditionPageView(noSuchOuvrage);
    const before = seen(wrapper);

    // When
    await flushPromises();

    // Then
    expect([before, seen(wrapper)]).toEqual([
      { card: 1, title: 0 },
      { card: 0, title: 0 },
    ]);
  });

  it("says there is no such ouvrage, alone", async () => {
    // Given
    const wrapper = mountEditionPageView(noSuchOuvrage);

    // When
    await flushPromises();

    // Then
    expect(normalised(wrapper.element)).toBe("Ouvrage introuvable.");
    expect(wrapper.findAllComponents(CircleAlert)).toHaveLength(1);
  });

  it("says to try again later when Libris does not answer, alone", async () => {
    // Given
    const wrapper = mountEditionPageView(
      new FakeCatalogueApi([], new TypeError("Failed to fetch")),
    );

    // When
    await flushPromises();

    // Then
    expect(normalised(wrapper.element)).toBe(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
    expect(wrapper.findAllComponents(CircleAlert)).toHaveLength(1);
  });

  it("shows the ouvrage's card, its title the page's h1", async () => {
    // Given
    const wrapper = mountEditionPageView({
      outcome: "found",
      book: asterixLeGaulois,
    });

    // When
    await flushPromises();

    // Then
    const screen = within(wrapper.element as HTMLElement);
    expect(screen.getAllByRole("heading")).toEqual([
      screen.getByRole("heading", { level: 1, name: "Astérix le Gaulois" }),
    ]);
  });

  it("shows the stored cover as the house's, with no button", async () => {
    // Given
    const wrapper = mountEditionPageView(
      { outcome: "found", book: romanceDawnOnlyViewed },
      romanceDawn.id,
    );

    // When
    await flushPromises();

    // Then
    const screen = within(wrapper.element as HTMLElement);
    const cover = screen.getByRole("img", {
      name: "Couverture de Romance dawn",
    });
    expect(cover.getAttribute("src")).toBe(romanceDawn.coverUrl);
    expect(screen.queryAllByRole("button")).toEqual([]);
  });

  it("shows the stand-in when no cover is stored", async () => {
    // Given
    const wrapper = mountEditionPageView({
      outcome: "found",
      book: asterixLeGaulois,
    });

    // When
    await flushPromises();

    // Then
    expect(
      within(wrapper.element as HTMLElement).queryAllByRole("img"),
    ).toEqual([]);
    expect(wrapper.findAllComponents(BookOpen)).toHaveLength(2);
  });

  it("asks the ouvrage of each id it is given", async () => {
    // Given
    const libris = new LibrisRecordingAsks();
    const wrapper = mountEditionPageView(libris, asterixLeGaulois.id);
    await flushPromises();

    // When
    await wrapper.setProps({ id: asterixEtSesAmis.id });
    await flushPromises();

    // Then
    expect(libris.asked).toEqual([asterixLeGaulois.id, asterixEtSesAmis.id]);
  });

  it("shows the card's skeleton again while another ouvrage comes", async () => {
    // Given
    const wrapper = mountEditionPageView(noSuchOuvrage, asterixLeGaulois.id);
    await flushPromises();

    // When
    await wrapper.setProps({ id: asterixEtSesAmis.id });
    const during = seen(wrapper);
    await flushPromises();

    // Then
    expect(during).toEqual({ card: 1, title: 0 });
    expect(normalised(wrapper.element)).toBe("Ouvrage introuvable.");
  });

  it("asks the ouvrage on each arrival", async () => {
    // Given
    const libris = new LibrisRecordingAsks();
    mountEditionPageView(libris).unmount();
    await flushPromises();

    // When
    mountEditionPageView(libris);
    await flushPromises();

    // Then
    expect(libris.asked).toEqual([asterixLeGaulois.id, asterixLeGaulois.id]);
  });

  it("removes one copy of the row whose Retirer is tapped", async () => {
    // Given
    const copiesApi = new FakeCopiesApi({ outcome: "removed" });
    const wrapper = mountEditionPageView(
      { outcome: "found", book: romanceDawnTwiceOnLea },
      romanceDawn.id,
      { copiesApi },
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    screen.getByRole("button", { name: "Retirer un exemplaire" }).click();
    await flushPromises();

    // When
    screen.getByRole("button", { name: "Retirer" }).click();
    await flushPromises();

    // Then
    expect(copiesApi.asked).toEqual([
      expect.toBeOneOf(romanceDawnTwiceOnLea.copies.map((copy) => copy.id)),
    ]);
  });

  it("shows the spinner while the removal is sent", async () => {
    // Given
    const wrapper = mountEditionPageView(
      { outcome: "found", book: romanceDawnTwiceOnLea },
      romanceDawn.id,
      { copiesApi: new LibrisNotRemoving() },
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    screen.getByRole("button", { name: "Retirer un exemplaire" }).click();
    await flushPromises();

    // When
    screen.getByRole("button", { name: "Retirer" }).click();
    await flushPromises();

    // Then
    expect(
      screen.getByRole("button", { name: "Retrait en cours…" }),
    ).toBeTruthy();
  });

  it("asks the ouvrage again once the copy is removed", async () => {
    // Given
    const wrapper = mountEditionPageView(
      new FakeCatalogueApi(
        [],
        [
          { outcome: "found", book: romanceDawnTwiceOnLea },
          { outcome: "found", book: romanceDawnOnceOnLea },
        ],
      ),
      romanceDawn.id,
      { copiesApi: new FakeCopiesApi({ outcome: "removed" }) },
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    screen.getByRole("button", { name: "Retirer un exemplaire" }).click();
    await flushPromises();

    // When
    screen.getByRole("button", { name: "Retirer" }).click();
    await flushPromises();

    // Then
    expect(screen.queryByText("2 exemplaires")).toBeNull();
    expect(screen.getByText("Dans Bibliothèque de Léa")).toBeTruthy();
  });

  it("keeps the card, its spinner turning, while the ouvrage is asked again", async () => {
    // Given
    const wrapper = mountEditionPageView(
      new LibrisAnsweringOnce({
        outcome: "found",
        book: romanceDawnTwiceOnLea,
      }),
      romanceDawn.id,
      { copiesApi: new FakeCopiesApi({ outcome: "removed" }) },
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    screen.getByRole("button", { name: "Retirer un exemplaire" }).click();
    await flushPromises();

    // When
    screen.getByRole("button", { name: "Retirer" }).click();
    await flushPromises();

    // Then
    expect(seen(wrapper)).toEqual({ card: 0, title: 0 });
    expect(
      screen.getByRole("button", { name: "Retrait en cours…" }),
    ).toBeTruthy();
  });

  it("says the removal failed when Libris does not answer", async () => {
    // Given
    const wrapper = mountEditionPageView(
      { outcome: "found", book: romanceDawnTwiceOnLea },
      romanceDawn.id,
      { copiesApi: new FakeCopiesApi(new TypeError("Failed to fetch")) },
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    screen.getByRole("button", { name: "Retirer un exemplaire" }).click();
    await flushPromises();

    // When
    screen.getByRole("button", { name: "Retirer" }).click();
    await flushPromises();

    // Then
    expect(
      screen.getByText("Erreur lors du retrait, veuillez réessayer plus tard."),
    ).toBeTruthy();
    expect(
      screen.getByText("Retirer un exemplaire de Bibliothèque de Léa ?"),
    ).toBeTruthy();
  });

  it("goes to the catalogue once the last copy is removed, opened from its address", async () => {
    // Given
    const router = await routerOn(romanceDawnPage);
    const screen = await askingToRemove(router);

    // When
    screen.getByRole("button", { name: "Retirer" }).click();

    // Then
    await waitFor(() => expect(window.location.pathname).toBe("/catalogue"));
  });

  it("goes back to the screen it was opened from once the last copy is removed", async () => {
    // Given
    const router = await routerOn("/catalogue/series", romanceDawnPage);
    const screen = await askingToRemove(router);

    // When
    screen.getByRole("button", { name: "Retirer" }).click();

    // Then
    await waitFor(() =>
      expect(window.location.pathname).toBe("/catalogue/series"),
    );
  });

  it("leaves the history when it goes to the catalogue", async () => {
    // Given
    const router = await routerOn(romanceDawnPage);
    const screen = await askingToRemove(router);
    const entries = window.history.length;

    // When
    screen.getByRole("button", { name: "Retirer" }).click();
    await waitFor(() => expect(window.location.pathname).toBe("/catalogue"));

    // Then
    expect(window.history.length).toBe(entries);
  });

  it("leaves the history when it goes back", async () => {
    // Given
    const router = await routerOn("/catalogue/series", romanceDawnPage);
    const screen = await askingToRemove(router);
    const entries = window.history.length;

    // When
    screen.getByRole("button", { name: "Retirer" }).click();
    await waitFor(() =>
      expect(window.location.pathname).toBe("/catalogue/series"),
    );

    // Then
    expect(window.history.length).toBe(entries);
  });

  it("keeps its spinner while it goes back", async () => {
    // Given
    const router = await routerOn("/catalogue/series", romanceDawnPage);
    const screen = await askingToRemove(router);

    // When
    screen.getByRole("button", { name: "Retirer" }).click();
    await flushPromises();

    // Then
    expect(window.location.pathname).toBe(romanceDawnPage);
    expect(
      screen.getByRole("button", { name: "Retrait en cours…" }),
    ).toBeTruthy();
  });

  it("asks nothing more once the last copy is removed", async () => {
    // Given
    const libris = new LibrisRecordingAsks({
      outcome: "found",
      book: romanceDawnOnceOnLea,
    });
    const router = await routerOn(romanceDawnPage);
    const screen = await askingToRemove(router, libris);

    // When
    screen.getByRole("button", { name: "Retirer" }).click();
    await waitFor(() => expect(window.location.pathname).toBe("/catalogue"));

    // Then
    expect(libris.asked).toEqual([romanceDawn.id]);
  });

  it("stays on the page while a copy is left on a guest's bookshelf", async () => {
    // Given
    const router = await routerOn(romanceDawnPage);
    const screen = await askingToRemove(
      router,
      new FakeCatalogueApi(
        [],
        [
          { outcome: "found", book: romanceDawnOnLeaAndViewedOnSalon },
          { outcome: "found", book: romanceDawnViewedOnSalon },
        ],
      ),
    );

    // When
    screen.getByRole("button", { name: "Retirer" }).click();
    await flushPromises();

    // Then
    expect(window.location.pathname).toBe(romanceDawnPage);
    expect(screen.queryByText(/Bibliothèque de Léa/)).toBeNull();
    expect(screen.getByText("Dans Salon")).toBeTruthy();
  });

  it("goes back too when the copy was already gone", async () => {
    // Given
    const router = await routerOn(romanceDawnPage);
    const screen = await askingToRemove(
      router,
      { outcome: "found", book: romanceDawnOnceOnLea },
      new FakeCopiesApi({ outcome: "problem", type: "/problems/not-found" }),
    );

    // When
    screen.getByRole("button", { name: "Retirer" }).click();

    // Then
    await waitFor(() => expect(window.location.pathname).toBe("/catalogue"));
  });

  it("offers the pencil on the card whatever the reader's role", async () => {
    // Given
    const wrapper = mountEditionPageView({
      outcome: "found",
      book: asterixWithIsbnOnlyViewed,
    });

    // When
    await flushPromises();

    // Then
    const screen = within(wrapper.element as HTMLElement);
    expect(screen.getAllByRole("button")).toEqual([
      screen.getByRole("button", { name: "Modifier" }),
    ]);
  });

  it("offers no pencil on an ouvrage with no ISBN", async () => {
    // Given
    const wrapper = mountEditionPageView({
      outcome: "found",
      book: asterixLeGaulois,
    });

    // When
    await flushPromises();

    // Then
    expect(
      within(wrapper.element as HTMLElement).queryByRole("button", {
        name: "Modifier",
      }),
    ).toBeNull();
  });

  it("shows the form in place of the card on the pencil", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn);

    // When
    await tapPencil(screen);

    // Then
    expect(
      screen.getByRole<HTMLInputElement>("textbox", { name: "Titre" }).value,
    ).toBe("Astérix le Gaulois");
    expect(screen.getByText("9782012101333")).toBeDefined();
    expect(
      screen.getByRole("button", { name: "Annuler les modifications" }),
    ).toBeDefined();
    expect(screen.queryByRole("button", { name: "Modifier" })).toBeNull();
  });

  it("keeps the answered title as the page's heading while the form shows", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn);
    await tapPencil(screen);

    // When
    await typeTitle(screen, "Astérix le gaulois");

    // Then
    expect(screen.getAllByRole("heading")).toEqual([
      screen.getByRole("heading", { level: 1, name: "Astérix le Gaulois" }),
    ]);
  });

  it("says Existante in Série for a series of the house", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn, {
      seriesApi: new FakeSeriesApi([], undefined, ["Astérix"]),
    });

    // When
    await tapPencil(screen);

    // Then
    expect(screen.getByText("Existante")).toBeDefined();
  });

  it("says Existant for an author of the house", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn, {
      authorsApi: new FakeAuthorsApi(["René Goscinny"]),
    });

    // When
    await tapPencil(screen);

    // Then
    expect(screen.getAllByText("Existant")).toHaveLength(1);
  });

  it("shows the stored cover in the form with no dot", async () => {
    // Given
    const screen = await pageOf(romanceDawnWithIsbn);

    // When
    await tapPencil(screen);

    // Then
    const cover = screen.getByRole("img", {
      name: "Couverture de Romance dawn",
    });
    expect(cover.getAttribute("src")).toBe(romanceDawn.coverUrl);
    expect(screen.queryAllByRole("button", { name: /^Couverture / })).toEqual(
      [],
    );
  });

  it("shows the card again on the cross, the edits dropped", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn);
    await tapPencil(screen);
    await typeTitle(screen, "Astérix le gaulois");

    // When
    await tapCross(screen);

    // Then
    expect(screen.queryByRole("textbox", { name: "Titre" })).toBeNull();
    expect(screen.queryByRole("button", { name: "Enregistrer" })).toBeNull();
    expect(
      screen.queryByRole("button", { name: "Annuler les modifications" }),
    ).toBeNull();
    expect(screen.getByRole("button", { name: "Modifier" })).toBeDefined();
    expect(
      screen.getByRole("button", { name: "Retirer un exemplaire" }),
    ).toBeDefined();
  });

  it("reopens the form as answered after the cross", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn);
    await tapPencil(screen);
    await typeTitle(screen, "Astérix le gaulois");
    await tapCross(screen);

    // When
    await tapPencil(screen);

    // Then
    expect(
      screen.getByRole<HTMLInputElement>("textbox", { name: "Titre" }).value,
    ).toBe("Astérix le Gaulois");
  });

  it("saves the edits under the ouvrage's id", async () => {
    // Given
    const editionsApi = new FakeEditionsApi({
      outcome: "saved",
      book: asterixWithIsbn,
    });
    const screen = await pageOf(asterixWithIsbn, { editionsApi });
    await tapPencil(screen);
    await typeTitle(screen, "Astérix le gaulois");

    // When
    await tapSave(screen);

    // Then
    expect(
      editionsApi.asked.map(({ id, edition }) => [id, edition.title]),
    ).toEqual([[asterixWithIsbn.id, "Astérix le gaulois"]]);
  });

  it("says Enregistrement en cours… while the save runs, the fields accepting nothing", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn, {
      editionsApi: new LibrisNotSaving(),
    });
    await tapPencil(screen);
    await typeTitle(screen, "Astérix le gaulois");

    // When
    await tapSave(screen);

    // Then
    expect(
      screen.getByRole("button", { name: "Enregistrement en cours…" }),
    ).toBeDefined();
    expect(
      screen.getByRole<HTMLInputElement>("textbox", { name: "Titre" }).disabled,
    ).toBe(true);
  });

  it("keeps the cross from dropping the edits while the save runs", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn, {
      editionsApi: new LibrisNotSaving(),
    });
    await tapPencil(screen);
    await typeTitle(screen, "Astérix le gaulois");

    // When
    await tapSave(screen);

    // Then
    expect(
      screen.getByRole<HTMLButtonElement>("button", {
        name: "Annuler les modifications",
      }).disabled,
    ).toBe(true);
  });

  it("closes the form once saved", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn, {
      editionsApi: new FakeEditionsApi({
        outcome: "saved",
        book: asterixWithIsbn,
      }),
    });
    await tapPencil(screen);
    await typeTitle(screen, "Astérix le gaulois");

    // When
    await tapSave(screen);

    // Then
    expect(screen.queryByRole("textbox", { name: "Titre" })).toBeNull();
    expect(screen.queryByRole("button", { name: "Enregistrer" })).toBeNull();
    expect(screen.getByRole("button", { name: "Modifier" })).toBeDefined();
  });

  it("shows the card as Libris answered the save", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn, {
      editionsApi: new FakeEditionsApi({
        outcome: "saved",
        book: { ...asterixWithIsbn, subtitle: "Une aventure d'Astérix" },
      }),
    });
    await tapPencil(screen);
    await typeTitle(screen, "Astérix le gaulois");

    // When
    await tapSave(screen);

    // Then
    expect(screen.getByText("Une aventure d'Astérix")).toBeDefined();
  });

  it("says the save failed when Libris refuses it", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn, { editionsApi: refused() });
    await tapPencil(screen);
    await typeTitle(screen, "Astérix le gaulois");

    // When
    await tapSave(screen);

    // Then
    expect(
      screen.getByText(
        "Erreur lors de l'enregistrement, veuillez réessayer plus tard.",
      ),
    ).toBeDefined();
  });

  it("keeps the form as left once the save failed", async () => {
    // Given
    const screen = await pageOf(asterixWithIsbn, { editionsApi: refused() });
    await tapPencil(screen);
    await typeTitle(screen, "Astérix le gaulois");

    // When
    await tapSave(screen);

    // Then
    const title = screen.getByRole<HTMLInputElement>("textbox", {
      name: "Titre",
    });
    expect(title.value).toBe("Astérix le gaulois");
    expect(title.disabled).toBe(false);
    expect(screen.getByRole("button", { name: "Enregistrer" })).toBeDefined();
  });
});

function seen(wrapper: ReturnType<typeof mountEditionPageView>) {
  return {
    card: wrapper.findAllComponents(HeldEditionCardSkeleton).length,
    title: wrapper.findAllComponents(PageTitleSkeleton).length,
  };
}

async function routerOn(...paths: string[]) {
  const router = createLibrisRouter();
  for (const path of paths) await router.push(path);
  await router.isReady();
  return router;
}

async function pageOf(
  book: Book,
  ports: {
    seriesApi?: SeriesApi;
    authorsApi?: AuthorsApi;
    editionsApi?: EditionsApi;
  } = {},
) {
  const wrapper = mountEditionPageView(
    { outcome: "found", book },
    book.id,
    ports,
  );
  await flushPromises();
  return within(wrapper.element as HTMLElement);
}

async function tapPencil(screen: ReturnType<typeof within>) {
  screen.getByRole("button", { name: "Modifier" }).click();
  await flushPromises();
}

async function tapCross(screen: ReturnType<typeof within>) {
  screen.getByRole("button", { name: "Annuler les modifications" }).click();
  await flushPromises();
}

async function tapSave(screen: ReturnType<typeof within>) {
  screen.getByRole("button", { name: "Enregistrer" }).click();
  await flushPromises();
}

async function typeTitle(screen: ReturnType<typeof within>, title: string) {
  await fireEvent.input(screen.getByRole("textbox", { name: "Titre" }), {
    target: { value: title },
  });
}

function refused() {
  return new FakeEditionsApi({
    outcome: "problem",
    type: "/problems/validation",
  });
}

async function askingToRemove(
  router: Router,
  catalogue: FindBookAnswer | CatalogueApi = {
    outcome: "found",
    book: romanceDawnOnceOnLea,
  },
  copiesApi: CopiesApi = new FakeCopiesApi({ outcome: "removed" }),
) {
  const wrapper = mountEditionPageView(catalogue, romanceDawn.id, {
    copiesApi,
    router,
  });
  await flushPromises();
  const screen = within(wrapper.element as HTMLElement);
  screen.getByRole("button", { name: "Retirer un exemplaire" }).click();
  await flushPromises();
  return screen;
}

const romanceDawnOnlyViewed: Book = {
  ...romanceDawn,
  copies: romanceDawn.copies.map((copy) => ({
    ...copy,
    bookshelf: { ...copy.bookshelf, role: "VIEWER" },
  })),
};

const romanceDawnTwiceOnLea: Book = {
  ...romanceDawn,
  copies: romanceDawn.copies.filter(
    (copy) => copy.bookshelf.id === bibliothequeDeLea.id,
  ),
};

const romanceDawnOnceOnLea: Book = {
  ...romanceDawn,
  copies: romanceDawnTwiceOnLea.copies.slice(1),
};

const romanceDawnPage = `/catalogue/ouvrages/${romanceDawn.id}`;

const romanceDawnViewedOnSalon: Book = {
  ...romanceDawn,
  copies: romanceDawn.copies
    .filter((copy) => copy.bookshelf.id === salon.id)
    .map((copy) => ({ ...copy, bookshelf: { ...salon, role: "VIEWER" } })),
};

const romanceDawnOnLeaAndViewedOnSalon: Book = {
  ...romanceDawn,
  copies: [...romanceDawnOnceOnLea.copies, ...romanceDawnViewedOnSalon.copies],
};

const asterixWithIsbn: Book = {
  ...asterixLeGaulois,
  isbn13: "9782012101333",
};

const asterixWithIsbnOnlyViewed: Book = {
  ...asterixWithIsbn,
  copies: asterixWithIsbn.copies.map((copy) => ({
    ...copy,
    bookshelf: { ...copy.bookshelf, role: "VIEWER" },
  })),
};

const romanceDawnWithIsbn: Book = {
  ...romanceDawn,
  isbn13: "9782723488525",
};

const noSuchOuvrage: FindBookAnswer = {
  outcome: "problem",
  type: "/problems/not-found",
};
