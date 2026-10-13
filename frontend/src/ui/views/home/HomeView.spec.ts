import { within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { bookshelvesApiKey } from "../../../application/BookshelvesApi";
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

const mountHomeView = (
  reader: Reader,
  memberships: Membership[] | Error = [],
) =>
  mount(HomeView, {
    global: {
      plugins: [createLibrisI18n(), createLibrisRouter()],
      provide: {
        [meApiKey]: new FakeMeApi(reader),
        [bookshelvesApiKey]: new FakeBookshelvesApi({ memberships }),
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

  function seen(wrapper: ReturnType<typeof mountHomeView>) {
    const screen = within(wrapper.element as HTMLElement);
    return {
      label: wrapper.text().includes("Mes bibliothèques"),
      list: screen.queryByRole("list", { name: "Mes bibliothèques" }) !== null,
      skeletonRows: wrapper.findAllComponents(BookshelfRowSkeleton).length,
    };
  }
});
