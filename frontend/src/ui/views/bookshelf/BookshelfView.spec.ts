import { fireEvent, within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import {
  bookshelvesApiKey,
  type FindAnswer,
} from "../../../application/BookshelvesApi";
import { meApiKey } from "../../../application/MeApi";
import { readersApiKey } from "../../../application/ReadersApi";
import type { KnownReader } from "../../../domain/KnownReader";
import type { Member } from "../../../domain/Member";
import { bibliothequeDeLea, salon } from "../../../fixture/Books";
import { FakeBookshelvesApi } from "../../../fixture/FakeBookshelvesApi";
import { FakeMeApi } from "../../../fixture/FakeMeApi";
import { FakeReadersApi } from "../../../fixture/FakeReadersApi";
import { lea } from "../../../fixture/Readers";
import { normalised } from "../../../fixture/normalised";
import MemberRowSkeleton from "../../components/MemberRowSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";
import { createLibrisI18n } from "../../i18n";
import BookshelfView from "./BookshelfView.vue";

const leaToOthers: KnownReader = { id: lea.id, displayName: lea.displayName };

const marc: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000005",
  displayName: "Marc",
};

const elodie: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000003",
  displayName: "Élodie",
};

const hugo: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000004",
  displayName: "Hugo",
};

const salonHeld: FindAnswer = {
  outcome: "found",
  bookshelf: salon,
  members: [viewer(leaToOthers), owner(marc), owner(elodie)],
};

const salonOwned: FindAnswer = {
  outcome: "found",
  bookshelf: salon,
  members: [owner(leaToOthers), viewer(marc)],
};

const mountBookshelfView = (
  find: FindAnswer | Error,
  readers: KnownReader[] = [],
  adds?: KnownReader,
) =>
  mount(BookshelfView, {
    props: { id: salon.id },
    global: {
      plugins: [createLibrisI18n()],
      provide: {
        [meApiKey]: new FakeMeApi(lea),
        [bookshelvesApiKey]: new FakeBookshelvesApi({
          memberships: [],
          find,
          adds,
        }),
        [readersApiKey]: new FakeReadersApi(readers),
      },
    },
  });

describe("BookshelfView", () => {
  it("names the bookshelf over its members, owners first", async () => {
    // Given
    const wrapper = mountBookshelfView(salonHeld);

    // When
    await flushPromises();

    // Then
    const screen = within(wrapper.element as HTMLElement);
    expect(screen.getByRole("heading", { name: "Salon" })).toBeDefined();
    expect(
      within(screen.getByRole("list", { name: "Membres" }))
        .getAllByRole("listitem")
        .map(normalised),
    ).toEqual([
      "Élodie · propriétaire",
      "Marc · propriétaire",
      "Vous · invité",
    ]);
  });

  it("shows skeletons while the bookshelf comes", async () => {
    // Given
    const wrapper = mountBookshelfView(salonHeld);
    const before = seen(wrapper);

    // When
    await flushPromises();

    // Then
    expect([before, seen(wrapper)]).toEqual([
      { label: true, heading: false, list: false, title: 1, rows: 2 },
      { label: true, heading: true, list: true, title: 0, rows: 0 },
    ]);
  });

  it("says there is no such bookshelf, alone", async () => {
    // Given
    const wrapper = mountBookshelfView({
      outcome: "problem",
      type: "/problems/not-found",
    });

    // When
    await flushPromises();

    // Then
    expect(normalised(wrapper.element)).toBe("Bibliothèque introuvable.");
  });

  it("says to try again later when Libris does not answer, alone", async () => {
    // Given
    const wrapper = mountBookshelfView(new TypeError("Failed to fetch"));

    // When
    await flushPromises();

    // Then
    expect(normalised(wrapper.element)).toBe(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
  });

  it("finds the bookshelf again when its id changes", async () => {
    // Given
    const wrapper = mountBookshelfView(salonHeld);
    await flushPromises();

    // When
    await wrapper.setProps({ id: bibliothequeDeLea.id });
    const during = seen(wrapper);
    await flushPromises();

    // Then
    expect(during).toEqual({
      label: true,
      heading: false,
      list: false,
      title: 1,
      rows: 2,
    });
    expect(normalised(wrapper.element)).toBe("Bibliothèque introuvable.");
  });

  it("adds the form as the last row of Membres", async () => {
    // Given
    const wrapper = mountBookshelfView(salonOwned, [
      leaToOthers,
      marc,
      hugo,
      elodie,
    ]);
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "Ajouter un membre" }),
    );

    // Then
    const last = within(screen.getByRole("list", { name: "Membres" }))
      .getAllByRole("listitem")
      .at(-1)!;
    const choice = within(last).getByRole("combobox", { name: "Lecteur" });
    expect(
      within(choice)
        .getAllByRole("option")
        .map((option) => option.textContent),
    ).toEqual(["Choisir un lecteur", "Élodie", "Hugo"]);
  });

  it("hides the plus while the form is there", async () => {
    // Given
    const wrapper = mountBookshelfView(salonOwned, [leaToOthers, marc, hugo]);
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "Ajouter un membre" }),
    );

    // Then
    expect(
      screen.queryByRole("button", { name: "Ajouter un membre" }),
    ).toBeNull();
  });

  function seen(wrapper: ReturnType<typeof mountBookshelfView>) {
    const screen = within(wrapper.element as HTMLElement);
    return {
      label: wrapper.text().includes("Membres"),
      heading: screen.queryByRole("heading", { name: "Salon" }) !== null,
      list: screen.queryByRole("list", { name: "Membres" }) !== null,
      title: wrapper.findAllComponents(PageTitleSkeleton).length,
      rows: wrapper.findAllComponents(MemberRowSkeleton).length,
    };
  }
});

function owner(reader: KnownReader): Member {
  return { reader, role: "OWNER" };
}

function viewer(reader: KnownReader): Member {
  return { reader, role: "VIEWER" };
}
