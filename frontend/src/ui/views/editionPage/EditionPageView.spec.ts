import { BookOpen, CircleAlert } from "@lucide/vue";
import { waitFor, within } from "@testing-library/dom";
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
  copiesApiKey,
  type CopiesApi,
  type RemoveAnswer,
} from "../../../application/CopiesApi";
import type { Book } from "../../../domain/Book";
import {
  asterixEtSesAmis,
  asterixLeGaulois,
  bibliothequeDeLea,
  romanceDawn,
  salon,
} from "../../../fixture/Books";
import { FakeCatalogueApi } from "../../../fixture/FakeCatalogueApi";
import { FakeCopiesApi } from "../../../fixture/FakeCopiesApi";
import { normalised } from "../../../fixture/normalised";
import HeldEditionCardSkeleton from "../../components/HeldEditionCardSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";
import { createLibrisI18n } from "../../i18n";
import { createLibrisRouter } from "../../router";
import EditionPageView from "./EditionPageView.vue";

const mountEditionPageView = (
  found: FindBookAnswer | CatalogueApi,
  id = asterixLeGaulois.id,
  copiesApi: CopiesApi = new FakeCopiesApi(new Error("no removal here")),
  router: Router = createLibrisRouter(),
) =>
  mount(EditionPageView, {
    props: { id },
    global: {
      plugins: [createLibrisI18n(), router],
      provide: {
        [catalogueApiKey]:
          "outcome" in found ? new FakeCatalogueApi([], found) : found,
        [copiesApiKey]: copiesApi,
      },
    },
  });

class LibrisNotRemoving implements CopiesApi {
  remove(): Promise<RemoveAnswer> {
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
      copiesApi,
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
      new LibrisNotRemoving(),
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
      new FakeCopiesApi({ outcome: "removed" }),
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
      new FakeCopiesApi({ outcome: "removed" }),
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
      new FakeCopiesApi(new TypeError("Failed to fetch")),
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
});

async function askingToRemove(
  router: Router,
  catalogue: FindBookAnswer | CatalogueApi = {
    outcome: "found",
    book: romanceDawnOnceOnLea,
  },
  copiesApi: CopiesApi = new FakeCopiesApi({ outcome: "removed" }),
) {
  const wrapper = mountEditionPageView(
    catalogue,
    romanceDawn.id,
    copiesApi,
    router,
  );
  await flushPromises();
  const screen = within(wrapper.element as HTMLElement);
  screen.getByRole("button", { name: "Retirer un exemplaire" }).click();
  await flushPromises();
  return screen;
}

async function routerOn(...paths: string[]) {
  const router = createLibrisRouter();
  for (const path of paths) await router.push(path);
  await router.isReady();
  return router;
}

function seen(wrapper: ReturnType<typeof mountEditionPageView>) {
  return {
    card: wrapper.findAllComponents(HeldEditionCardSkeleton).length,
    title: wrapper.findAllComponents(PageTitleSkeleton).length,
  };
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

const romanceDawnPage = `/catalogue/ouvrages/${romanceDawn.id}`;

const noSuchOuvrage: FindBookAnswer = {
  outcome: "problem",
  type: "/problems/not-found",
};
