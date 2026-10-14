import { fireEvent, within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import {
  bookshelvesApiKey,
  type ChooseDefaultAnswer,
  type CreateAnswer,
} from "../../../application/BookshelvesApi";
import { meApiKey } from "../../../application/MeApi";
import { revisionKey } from "../../../application/Revision";
import type { Bookshelf } from "../../../domain/Bookshelf";
import type { Membership } from "../../../domain/Membership";
import type { Reader } from "../../../domain/Reader";
import { salon } from "../../../fixture/Books";
import { FakeBookshelvesApi } from "../../../fixture/FakeBookshelvesApi";
import { FakeMeApi } from "../../../fixture/FakeMeApi";
import { chloe } from "../../../fixture/Readers";
import { normalised } from "../../../fixture/normalised";
import BookshelfRowSkeleton from "../../components/BookshelfRowSkeleton.vue";
import { createLibrisI18n } from "../../i18n";
import { createLibrisRouter } from "../../router";
import HomeView from "./HomeView.vue";

const etagere: Bookshelf = {
  id: "2d3a4f5e-6b7c-4d8e-8f9a-1b2c3d4e5f60",
  name: "Étagère",
};

const atelier: Bookshelf = {
  id: "7e8f9a0b-1c2d-4e3f-8a5b-6c7d8e9f0a1b",
  name: "Atelier",
};

class LibrisNotAnswering extends FakeBookshelvesApi {
  override create(): Promise<CreateAnswer> {
    return new Promise(() => {});
  }

  override chooseDefault(): Promise<ChooseDefaultAnswer> {
    return new Promise(() => {});
  }
}

const mountHomeView = (
  reader: Reader,
  memberships: Membership[] | Error = [],
  create?: CreateAnswer | Error,
  bookshelvesApi = new FakeBookshelvesApi({ memberships, create }),
) =>
  mount(HomeView, {
    global: {
      plugins: [createLibrisI18n(), createLibrisRouter()],
      provide: {
        [meApiKey]: new FakeMeApi(reader),
        [bookshelvesApiKey]: bookshelvesApi,
        [revisionKey]: "sha-abc1234",
      },
    },
  });

describe("HomeView", () => {
  it("renders the French title from the messages", () => {
    const wrapper = mountHomeView(chloe);

    expect(wrapper.text()).toContain("Libris");
  });

  it("greets the reader the API answers by display name", async () => {
    // Given
    const wrapper = mountHomeView(chloe);

    // When
    await flushPromises();

    // Then
    expect(wrapper.text()).toContain("Bonjour Chloé");
  });

  it("has no link to add an ouvrage", async () => {
    // Given
    const wrapper = mountHomeView(chloe);

    // When
    await flushPromises();

    // Then
    const screen = within(wrapper.element as HTMLElement);
    expect(
      screen.queryByRole("link", { name: "Ajouter un ouvrage" }),
    ).toBeNull();
  });

  it("shows the revision at the foot of the page", async () => {
    // Given
    const wrapper = mountHomeView(chloe);

    // When
    await flushPromises();

    // Then
    const screen = within(wrapper.element as HTMLElement);
    const revision = screen.getByText("sha-abc1234");
    const greeting = screen.getByText("Bonjour Chloé");
    expect(greeting.compareDocumentPosition(revision)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING,
    );
  });

  it("lists the reader's bookshelves under Mes bibliothèques, the default first", async () => {
    // Given
    const wrapper = mountHomeView(chloe, [
      { bookshelf: salon, role: "VIEWER" },
      { bookshelf: etagere, role: "OWNER" },
      { bookshelf: chloe.defaultBookshelf, role: "OWNER" },
    ]);

    // When
    await flushPromises();

    // Then
    const screen = within(wrapper.element as HTMLElement);
    expect(
      within(screen.getByRole("list", { name: "Mes bibliothèques" }))
        .getAllByRole("listitem")
        .map(normalised),
    ).toEqual([
      "Bibliothèque de Chloé propriétaire · par défaut",
      "Étagère propriétaire",
      "Salon invité",
    ]);
  });

  it("draws skeleton rows under the label while the bookshelves come", async () => {
    // Given
    const wrapper = mountHomeView(chloe, [
      { bookshelf: salon, role: "VIEWER" },
    ]);
    const before = seen(wrapper);

    // When
    await flushPromises();

    // Then
    expect([before, seen(wrapper)]).toEqual([
      { label: true, list: false, skeletonRows: 3 },
      { label: true, list: true, skeletonRows: 0 },
    ]);
  });

  it("says the bookshelves cannot load when Libris does not answer", async () => {
    // Given
    const wrapper = mountHomeView(chloe, new TypeError("Failed to fetch"));

    // When
    await flushPromises();

    // Then
    const screen = within(wrapper.element as HTMLElement);
    expect(
      screen.getByText(
        "Erreur lors du chargement, veuillez réessayer plus tard.",
      ),
    ).toBeDefined();
    expect(
      screen.queryByRole("list", { name: "Mes bibliothèques" }),
    ).toBeNull();
    expect(wrapper.findAllComponents(BookshelfRowSkeleton)).toHaveLength(0);
  });

  it("offers Nouvelle bibliothèque once the rows are there", async () => {
    // Given
    const wrapper = mountHomeView(chloe, [
      { bookshelf: salon, role: "VIEWER" },
    ]);
    const before = offersNew(wrapper);

    // When
    await flushPromises();

    // Then
    expect([before, offersNew(wrapper)]).toEqual([false, true]);
  });

  it("opens the form as the list's last row, the plus gone", async () => {
    // Given
    const wrapper = mountHomeView(chloe, [
      { bookshelf: salon, role: "VIEWER" },
    ]);
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "Nouvelle bibliothèque" }),
    );

    // Then
    const last = within(screen.getByRole("list", { name: "Mes bibliothèques" }))
      .getAllByRole("listitem")
      .at(-1)!;
    expect(
      within(last).queryByRole("textbox", { name: "Nom de la bibliothèque" }),
    ).not.toBeNull();
    expect(offersNew(wrapper)).toBe(false);
  });

  it("lists the bookshelf created in order, the form gone, the plus back", async () => {
    // Given
    const wrapper = mountHomeView(
      chloe,
      [
        { bookshelf: salon, role: "VIEWER" },
        { bookshelf: chloe.defaultBookshelf, role: "OWNER" },
      ],
      { outcome: "created", bookshelf: atelier },
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    await nameNewBookshelf(screen, "Atelier");

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Créer" }));
    await flushPromises();

    // Then
    expect(
      within(screen.getByRole("list", { name: "Mes bibliothèques" }))
        .getAllByRole("listitem")
        .map(normalised),
    ).toEqual([
      "Bibliothèque de Chloé propriétaire · par défaut",
      "Atelier propriétaire",
      "Salon invité",
    ]);
    expect(offersNew(wrapper)).toBe(true);
  });

  it("is busy while the bookshelf is created", async () => {
    // Given
    const memberships: Membership[] = [{ bookshelf: salon, role: "VIEWER" }];
    const wrapper = mountHomeView(
      chloe,
      memberships,
      undefined,
      new LibrisNotAnswering({ memberships }),
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    await nameNewBookshelf(screen, "Atelier");

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Créer" }));
    await flushPromises();

    // Then
    expect(
      screen.queryByRole("button", { name: "Création en cours…" }),
    ).not.toBeNull();
  });

  it("says the name is taken in the form's row, the field keeping it", async () => {
    // Given
    const wrapper = mountHomeView(
      chloe,
      [
        { bookshelf: salon, role: "VIEWER" },
        { bookshelf: chloe.defaultBookshelf, role: "OWNER" },
      ],
      { outcome: "problem", type: "/problems/conflict" },
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    await nameNewBookshelf(screen, "salon");

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Créer" }));
    await flushPromises();

    // Then
    expect(
      within(screen.getByRole("list", { name: "Mes bibliothèques" }))
        .getAllByRole("listitem")
        .map(normalised),
    ).toEqual([
      "Bibliothèque de Chloé propriétaire · par défaut",
      "Salon invité",
      "Vous avez déjà une bibliothèque de ce nom.",
    ]);
    expect(
      screen.getByRole<HTMLInputElement>("textbox", {
        name: "Nom de la bibliothèque",
      }).value,
    ).toBe("salon");
    expect(offersNew(wrapper)).toBe(false);
  });

  it("says to try again later in the form's row when Libris does not answer", async () => {
    // Given
    const wrapper = mountHomeView(
      chloe,
      [
        { bookshelf: salon, role: "VIEWER" },
        { bookshelf: chloe.defaultBookshelf, role: "OWNER" },
      ],
      new TypeError("Failed to fetch"),
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    await nameNewBookshelf(screen, "salon");

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Créer" }));
    await flushPromises();

    // Then
    expect(
      within(screen.getByRole("list", { name: "Mes bibliothèques" }))
        .getAllByRole("listitem")
        .map(normalised),
    ).toEqual([
      "Bibliothèque de Chloé propriétaire · par défaut",
      "Salon invité",
      "Erreur lors de la création, veuillez réessayer plus tard.",
    ]);
    expect(
      screen.getByRole<HTMLInputElement>("textbox", {
        name: "Nom de la bibliothèque",
      }).value,
    ).toBe("salon");
    expect(offersNew(wrapper)).toBe(false);
  });

  it("makes the tapped bookshelf the default, first and marked", async () => {
    // Given
    const memberships: Membership[] = [
      { bookshelf: salon, role: "VIEWER" },
      { bookshelf: atelier, role: "OWNER" },
      { bookshelf: chloe.defaultBookshelf, role: "OWNER" },
    ];
    const wrapper = mountHomeView(
      chloe,
      memberships,
      undefined,
      new FakeBookshelvesApi({ memberships, defaultChosen: () => {} }),
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);

    // When
    await fireEvent.click(starOf(screen, "Atelier"));
    await flushPromises();

    // Then
    expect(rows(screen)).toEqual([
      "Atelier propriétaire · par défaut",
      "Bibliothèque de Chloé propriétaire",
      "Salon invité",
    ]);
  });

  it("is busy in the tapped row while the default is changed", async () => {
    // Given
    const memberships: Membership[] = [
      { bookshelf: chloe.defaultBookshelf, role: "OWNER" },
      { bookshelf: atelier, role: "OWNER" },
      { bookshelf: etagere, role: "OWNER" },
    ];
    const wrapper = mountHomeView(
      chloe,
      memberships,
      undefined,
      new LibrisNotAnswering({ memberships }),
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);

    // When
    await fireEvent.click(starOf(screen, "Atelier"));
    await flushPromises();

    // Then
    expect(
      rowOf(screen, "Atelier").queryByRole("button", {
        name: "Changement en cours…",
      }),
    ).not.toBeNull();
  });

  it("lets no other star be tapped while the default is changed", async () => {
    // Given
    const memberships: Membership[] = [
      { bookshelf: chloe.defaultBookshelf, role: "OWNER" },
      { bookshelf: atelier, role: "OWNER" },
      { bookshelf: etagere, role: "OWNER" },
    ];
    const wrapper = mountHomeView(
      chloe,
      memberships,
      undefined,
      new LibrisNotAnswering({ memberships }),
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);

    // When
    await fireEvent.click(starOf(screen, "Atelier"));
    await flushPromises();

    // Then
    expect(starOf(screen, "Étagère").disabled).toBe(true);
  });

  it("says Libris did not answer in the tapped row, the stars as they were", async () => {
    // Given
    const memberships: Membership[] = [
      { bookshelf: chloe.defaultBookshelf, role: "OWNER" },
      { bookshelf: atelier, role: "OWNER" },
    ];
    const wrapper = mountHomeView(
      chloe,
      memberships,
      undefined,
      new FakeBookshelvesApi({
        memberships,
        defaultChosen: new TypeError("Failed to fetch"),
      }),
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);

    // When
    await fireEvent.click(starOf(screen, "Atelier"));
    await flushPromises();

    // Then
    expect(rows(screen)).toEqual([
      "Bibliothèque de Chloé propriétaire · par défaut",
      "Atelier propriétaire Erreur lors du changement, veuillez réessayer plus tard.",
    ]);
    expect(starOf(screen, "Atelier").disabled).toBe(false);
  });

  function seen(wrapper: ReturnType<typeof mountHomeView>) {
    const screen = within(wrapper.element as HTMLElement);
    return {
      label: wrapper.text().includes("Mes bibliothèques"),
      list: screen.queryByRole("list", { name: "Mes bibliothèques" }) !== null,
      skeletonRows: wrapper.findAllComponents(BookshelfRowSkeleton).length,
    };
  }

  function offersNew(wrapper: ReturnType<typeof mountHomeView>) {
    return (
      within(wrapper.element as HTMLElement).queryByRole("button", {
        name: "Nouvelle bibliothèque",
      }) !== null
    );
  }

  function rows(screen: ReturnType<typeof within>) {
    return within(screen.getByRole("list", { name: "Mes bibliothèques" }))
      .getAllByRole("listitem")
      .map(normalised);
  }

  function rowOf(screen: ReturnType<typeof within>, name: string) {
    return within(
      within(screen.getByRole("list", { name: "Mes bibliothèques" }))
        .getAllByRole("listitem")
        .find((item) => within(item).queryByText(name) !== null)!,
    );
  }

  function starOf(screen: ReturnType<typeof within>, name: string) {
    return rowOf(screen, name).getByRole<HTMLButtonElement>("button", {
      name: "Choisir comme bibliothèque par défaut",
    });
  }

  async function nameNewBookshelf(
    screen: ReturnType<typeof within>,
    name: string,
  ) {
    await fireEvent.click(
      screen.getByRole("button", { name: "Nouvelle bibliothèque" }),
    );
    await fireEvent.input(
      screen.getByRole("textbox", { name: "Nom de la bibliothèque" }),
      { target: { value: name } },
    );
  }
});
