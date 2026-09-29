import { within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
  catalogueApiKey,
  type BookPage,
  type CatalogueApi,
} from "../../../application/CatalogueApi";
import {
  asterixEtSesAmis,
  asterixLeGaulois,
  romanceDawn,
} from "../../../fixture/Books";
import { FakeCatalogueApi } from "../../../fixture/FakeCatalogueApi";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
import IconAlert from "../../components/icons/IconAlert.vue";
import IconBook from "../../components/icons/IconBook.vue";
import { createLibrisI18n } from "../../i18n";
import CatalogueView from "./CatalogueView.vue";

describe("CatalogueView", () => {
  let watched: {
    callback: IntersectionObserverCallback;
    targets: Element[];
  }[] = [];

  beforeEach(() => {
    watched = [];
    vi.stubGlobal(
      "IntersectionObserver",
      class {
        private readonly targets: Element[] = [];
        constructor(callback: IntersectionObserverCallback) {
          watched.push({ callback, targets: this.targets });
        }
        observe(target: Element) {
          this.targets.push(target);
        }
        unobserve(target: Element) {
          const at = this.targets.indexOf(target);
          if (at >= 0) this.targets.splice(at, 1);
        }
        disconnect() {
          this.targets.length = 0;
        }
      },
    );
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("shows the title and the hint", () => {
    // When
    const { screen } = open(new FakeCatalogueApi([{ books: [], next: null }]));

    // Then
    expect(
      screen.getByRole("heading", { name: "Parcourir le catalogue" }),
    ).toBeDefined();
    expect(
      screen.getByText("Les ouvrages de toutes vos bibliothèques."),
    ).toBeDefined();
  });

  it("shows five skeleton rows while the first page is coming", () => {
    // When
    const { wrapper } = open(new FakeCatalogueApi([{ books: [], next: null }]));

    // Then
    expect(wrapper.findAllComponents(CatalogueRowSkeleton)).toHaveLength(5);
  });

  it("shows no skeleton row once the first page has come", async () => {
    // Given
    const { wrapper } = open(new FakeCatalogueApi([{ books: [], next: null }]));

    // When
    await flushPromises();

    // Then
    expect(wrapper.findAllComponents(CatalogueRowSkeleton)).toHaveLength(0);
  });

  it("says what will fill the catalogue when it is empty", async () => {
    // Given
    const { wrapper, screen } = open(
      new FakeCatalogueApi([{ books: [], next: null }]),
    );

    // When
    await flushPromises();

    // Then
    expect(
      screen.getByText("Les ouvrages de vos bibliothèques apparaîtront ici."),
    ).toBeDefined();
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(1);
  });

  it("lists one row per book of the first page", async () => {
    // Given
    const { wrapper, screen } = open(
      new FakeCatalogueApi([
        {
          books: [asterixLeGaulois, asterixEtSesAmis, romanceDawn],
          next: null,
        },
      ]),
    );

    // When
    await flushPromises();

    // Then
    expect(screen.getAllByRole("listitem")).toHaveLength(3);
    expect(wrapper.findAllComponents(CatalogueRowSkeleton)).toHaveLength(0);
    expect(
      screen.queryByText("Les ouvrages de vos bibliothèques apparaîtront ici."),
    ).toBeNull();
  });

  it("keeps the order of the answer", async () => {
    // Given
    const { screen } = open(
      new FakeCatalogueApi([
        { books: [romanceDawn, asterixLeGaulois], next: null },
      ]),
    );

    // When
    await flushPromises();

    // Then
    const [first, second] = screen.getAllByRole("listitem");
    expect(within(first!).getByText("Romance dawn")).toBeDefined();
    expect(within(second!).getByText("Astérix le Gaulois")).toBeDefined();
  });

  it("offers nothing to follow on a row", async () => {
    // Given
    const { screen } = open(
      new FakeCatalogueApi([
        { books: [asterixLeGaulois, romanceDawn], next: null },
      ]),
    );

    // When
    await flushPromises();

    // Then
    expect(screen.queryAllByRole("link")).toEqual([]);
    expect(screen.queryAllByRole("button")).toEqual([]);
  });

  it("says to try again later when the first page does not come", async () => {
    // Given
    const { wrapper, screen } = open(
      new FakeCatalogueApi([new TypeError("Failed to fetch")]),
    );

    // When
    await flushPromises();

    // Then
    expect(
      screen.getByText(
        "Erreur lors du chargement, veuillez réessayer plus tard.",
      ),
    ).toBeDefined();
    expect(wrapper.findAllComponents(IconAlert)).toHaveLength(1);
  });

  it("lists nothing when the first page does not come", async () => {
    // Given
    const { wrapper, screen } = open(
      new FakeCatalogueApi([new TypeError("Failed to fetch")]),
    );

    // When
    await flushPromises();

    // Then
    expect(wrapper.findAllComponents(CatalogueRowSkeleton)).toHaveLength(0);
    expect(screen.queryAllByRole("listitem")).toEqual([]);
    expect(
      screen.queryByText("Les ouvrages de vos bibliothèques apparaîtront ici."),
    ).toBeNull();
  });

  it("asks for the first page on each arrival", async () => {
    // Given
    const catalogueApi = new FakeCatalogueApi([
      { books: [], next: null },
      { books: [], next: null },
    ]);
    open(catalogueApi).wrapper.unmount();
    await flushPromises();

    // When
    open(catalogueApi);
    await flushPromises();

    // Then
    expect(catalogueApi.asked).toEqual([null, null]);
  });

  it("shows two skeleton rows under the rows while the next page is coming", async () => {
    // Given
    const { wrapper, screen } = open({
      browse: (after) =>
        after === null
          ? Promise.resolve(firstPage())
          : new Promise<BookPage>(() => {}),
    });
    await flushPromises();

    // When
    theLastRowComes(true);
    await flushPromises();

    // Then
    expect(wrapper.findAllComponents(CatalogueRowSkeleton)).toHaveLength(2);
    expect(screen.getAllByRole("listitem")).toHaveLength(2);
  });

  it("asks nothing while the last row is out of view", async () => {
    // Given
    const catalogueApi = new FakeCatalogueApi([firstPage(), lastPage()]);
    const { wrapper } = open(catalogueApi);
    await flushPromises();

    // When
    theLastRowComes(false);
    await flushPromises();

    // Then
    expect(wrapper.findAllComponents(CatalogueRowSkeleton)).toHaveLength(0);
    expect(catalogueApi.asked).toEqual([null]);
  });

  it("watches the last row alone", async () => {
    // Given
    open(
      new FakeCatalogueApi([
        { books: [asterixLeGaulois], next: asterixLeGaulois.id },
        { books: [asterixEtSesAmis], next: asterixEtSesAmis.id },
        { books: [romanceDawn], next: null },
      ]),
    );
    await flushPromises();
    theLastRowComes(true);
    await flushPromises();

    // When
    theLastRowComes(true);
    await flushPromises();

    // Then
    const targets = watchedTargets();
    expect(targets).toHaveLength(1);
    expect(
      within(targets[0] as HTMLElement).getByText("Romance dawn"),
    ).toBeDefined();
  });

  it("watches nothing once the page is left", async () => {
    // Given
    const { wrapper } = open(new FakeCatalogueApi([firstPage()]));
    await flushPromises();

    // When
    wrapper.unmount();

    // Then
    expect(watchedTargets()).toEqual([]);
  });

  it("says to try again later under the rows when the next page does not come", async () => {
    // Given
    const { wrapper, screen } = open(
      new FakeCatalogueApi([firstPage(), new TypeError("Failed to fetch")]),
    );
    await flushPromises();

    // When
    theLastRowComes(true);
    await flushPromises();

    // Then
    expect(
      screen.getByText(
        "Erreur lors du chargement, veuillez réessayer plus tard.",
      ),
    ).toBeDefined();
    expect(wrapper.findAllComponents(IconAlert)).toHaveLength(1);
    expect(screen.getAllByRole("listitem")).toHaveLength(2);
    expect(wrapper.findAllComponents(CatalogueRowSkeleton)).toHaveLength(0);
  });

  function firstPage(): BookPage {
    return {
      books: [asterixLeGaulois, asterixEtSesAmis],
      next: asterixEtSesAmis.id,
    };
  }

  function lastPage(): BookPage {
    return { books: [romanceDawn], next: null };
  }

  function open(catalogueApi: CatalogueApi) {
    const wrapper = mount(CatalogueView, {
      global: {
        plugins: [createLibrisI18n()],
        provide: { [catalogueApiKey]: catalogueApi },
      },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }

  function theLastRowComes(inView: boolean) {
    const observer = watched.at(-1);
    const target = observer?.targets.at(-1);
    if (observer === undefined || target === undefined)
      throw Error("No row is observed");
    observer.callback(
      [{ isIntersecting: inView, target } as IntersectionObserverEntry],
      {} as IntersectionObserver,
    );
  }

  function watchedTargets() {
    return watched.flatMap((observer) => observer.targets);
  }
});
