import { BookOpen, CircleAlert } from "@lucide/vue";
import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
  catalogueApiKey,
  type BookPage,
  type CatalogueApi,
} from "../../../application/CatalogueApi";
import { searchApiKey, type SearchApi } from "../../../application/SearchApi";
import {
  asterixEtSesAmis,
  asterixLeGaulois,
  romanceDawn,
} from "../../../fixture/Books";
import { FakeCatalogueApi } from "../../../fixture/FakeCatalogueApi";
import { FakeSearchApi } from "../../../fixture/FakeSearchApi";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
import { createLibrisI18n } from "../../i18n";
import { createLibrisRouter } from "../../router";
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

  it("shows the switch between the title and the ouvrages", async () => {
    // Given
    const { screen } = open(
      new FakeCatalogueApi([{ books: [asterixLeGaulois], next: null }]),
    );

    // When
    await flushPromises();

    // Then
    const title = screen.getByRole("heading", {
      name: "Parcourir le catalogue",
    });
    const viewSwitch = screen.getByRole("navigation", {
      name: "Vue du catalogue",
    });
    expect(under(title, viewSwitch)).toBe(true);
    expect(under(viewSwitch, screen.getByRole("list"))).toBe(true);
  });

  it("shows the search box under the switch, over the loading rows", () => {
    // When
    const { wrapper, screen } = open({
      browse: () => new Promise<BookPage>(() => {}),
      find: () => Promise.reject(Error("no find in this case")),
    });

    // Then
    const box = screen.getByRole("searchbox", {
      name: "Rechercher dans le catalogue",
    });
    expect(
      under(screen.getByRole("navigation", { name: "Vue du catalogue" }), box),
    ).toBe(true);
    expect(
      under(box, wrapper.findAllComponents(CatalogueRowSkeleton)[0]!.element),
    ).toBe(true);
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
    expect(wrapper.findAllComponents(BookOpen)).toHaveLength(1);
  });

  it("shows no search box over an empty catalogue", async () => {
    // Given
    const { screen } = open(new FakeCatalogueApi([{ books: [], next: null }]));

    // When
    await flushPromises();

    // Then
    expect(screen.queryByRole("searchbox")).toBeNull();
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

  it("lists the search's rows in place of the catalogue's", async () => {
    // Given
    const { screen } = open(
      new FakeCatalogueApi([
        { books: [asterixLeGaulois, asterixEtSesAmis], next: null },
      ]),
      new FakeSearchApi({
        goscinny: [
          {
            books: [romanceDawn, asterixLeGaulois],
            next: null,
            near: false,
          },
        ],
      }),
    );
    await flushPromises();

    // When
    const box = screen.getByRole<HTMLInputElement>("searchbox");
    await fireEvent.input(box, { target: { value: "goscinny" } });
    await fireEvent.submit(box.form!);
    await flushPromises();

    // Then
    const items = screen.getAllByRole("listitem");
    expect(items).toHaveLength(2);
    expect(within(items[0]!).getByText("Romance dawn")).toBeDefined();
    expect(within(items[1]!).getByText("Astérix le Gaulois")).toBeDefined();
  });

  it("says that no ouvrage matches the query asked", async () => {
    // Given
    const { screen } = open(
      new FakeCatalogueApi([
        { books: [asterixLeGaulois, asterixEtSesAmis], next: null },
      ]),
      new FakeSearchApi({
        gaston: [{ books: [], next: null, near: false }],
      }),
    );
    await flushPromises();

    // When
    await searchFor(screen, "gaston");

    // Then
    expect(
      screen.getByText("Aucun ouvrage ne correspond à « gaston »."),
    ).toBeDefined();
  });

  it("asks nothing while the reader types", async () => {
    // Given
    const searchApi = new FakeSearchApi({
      goscinny: [{ books: [romanceDawn], next: null, near: false }],
    });
    const { screen } = open(
      new FakeCatalogueApi([{ books: [asterixLeGaulois], next: null }]),
      searchApi,
    );
    await flushPromises();

    // When
    await fireEvent.input(screen.getByRole("searchbox"), {
      target: { value: "goscinny" },
    });
    await flushPromises();

    // Then
    expect(searchApi.asked).toEqual([]);
  });

  it("offers each row as one link, and no button", async () => {
    // Given
    const { screen } = open(
      new FakeCatalogueApi([
        { books: [asterixLeGaulois, romanceDawn], next: null },
      ]),
    );

    // When
    await flushPromises();

    // Then
    const list = within(screen.getByRole("list"));
    expect(
      list.queryAllByRole("link").map((link) => link.getAttribute("href")),
    ).toEqual([
      `/catalogue/ouvrages/${asterixLeGaulois.id}`,
      `/catalogue/ouvrages/${romanceDawn.id}`,
    ]);
    expect(list.queryAllByRole("button")).toEqual([]);
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
    expect(wrapper.findAllComponents(CircleAlert)).toHaveLength(1);
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
    expect(screen.queryByRole("list")).toBeNull();
    expect(
      screen.queryByText("Les ouvrages de vos bibliothèques apparaîtront ici."),
    ).toBeNull();
  });

  it("keeps the search box when the first page does not come", async () => {
    // Given
    const { screen } = open(
      new FakeCatalogueApi([new TypeError("Failed to fetch")]),
    );

    // When
    await flushPromises();

    // Then
    expect(screen.getAllByRole("searchbox")).toHaveLength(1);
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
      find: () => Promise.reject(Error("no find in this case")),
    });
    await flushPromises();

    // When
    theLastRowComes(true);
    await flushPromises();

    // Then
    const skeletons = wrapper.findAllComponents(CatalogueRowSkeleton);
    expect(skeletons).toHaveLength(2);
    expect(screen.getAllByRole("listitem")).toHaveLength(2);
    expect(under(screen.getByRole("list"), skeletons[0]!.element)).toBe(true);
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
    const message = screen.getByText(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
    expect(wrapper.findAllComponents(CircleAlert)).toHaveLength(1);
    expect(screen.getAllByRole("listitem")).toHaveLength(2);
    expect(wrapper.findAllComponents(CatalogueRowSkeleton)).toHaveLength(0);
    expect(under(screen.getByRole("list"), message)).toBe(true);
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

  function open(
    catalogueApi: CatalogueApi,
    searchApi: SearchApi = new FakeSearchApi(),
  ) {
    const wrapper = mount(CatalogueView, {
      global: {
        plugins: [createLibrisI18n(), createLibrisRouter()],
        provide: {
          [catalogueApiKey]: catalogueApi,
          [searchApiKey]: searchApi,
        },
      },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }

  async function searchFor(
    screen: BoundFunctions<typeof queries>,
    text: string,
  ) {
    const box = screen.getByRole<HTMLInputElement>("searchbox");
    await fireEvent.input(box, { target: { value: text } });
    await fireEvent.submit(box.form!);
    await flushPromises();
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

  function under(above: Element, below: Element) {
    return (
      (above.compareDocumentPosition(below) &
        Node.DOCUMENT_POSITION_FOLLOWING) !==
      0
    );
  }

  function watchedTargets() {
    return watched.flatMap((observer) => observer.targets);
  }
});
