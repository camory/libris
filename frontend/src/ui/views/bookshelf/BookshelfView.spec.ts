import {
  fireEvent,
  within,
  type BoundFunctions,
  type queries,
} from "@testing-library/dom";
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
  adds?: KnownReader | Error,
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
  it("names the bookshelf over its members, the reader first", async () => {
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
      "Vous · invité",
      "Élodie · propriétaire",
      "Marc · propriétaire",
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

  it("opens Choisir un lecteur under Membres", async () => {
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
    expect(
      within(screen.getByRole("list", { name: "Choisir un lecteur" }))
        .getAllByRole("button")
        .map((button) => button.textContent?.trim()),
    ).toEqual(["Élodie", "Hugo"]);
    expect(
      within(screen.getByRole("list", { name: "Membres" }))
        .getAllByRole("listitem")
        .map(normalised),
    ).toEqual(["Vous · propriétaire", "Marc · invité"]);
  });

  it("closes Choisir un lecteur on Fermer, the plus back", async () => {
    // Given
    const wrapper = mountBookshelfView(salonOwned, [leaToOthers, marc, hugo]);
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    await fireEvent.click(
      screen.getByRole("button", { name: "Ajouter un membre" }),
    );

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Fermer" }));

    // Then
    expect(
      screen.queryByRole("list", { name: "Choisir un lecteur" }),
    ).toBeNull();
    screen.getByRole("button", { name: "Ajouter un membre" });
  });

  it("hides the plus while the choice is there", async () => {
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

  it("shows the new member among the members, the choice gone", async () => {
    // Given
    const wrapper = mountBookshelfView(
      salonOwned,
      [leaToOthers, marc, hugo, elodie],
      hugo,
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    await chooseHugo(screen);
    await fireEvent.click(screen.getByRole("button", { name: "Propriétaire" }));

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Ajouter" }));
    await flushPromises();

    // Then
    expect(
      within(screen.getByRole("list", { name: "Membres" }))
        .getAllByRole("listitem")
        .map(normalised),
    ).toEqual(["Vous · propriétaire", "Hugo · propriétaire", "Marc · invité"]);
    expect(
      screen.queryByRole("list", { name: "Choisir un lecteur" }),
    ).toBeNull();
    screen.getByRole("button", { name: "Ajouter un membre" });
  });

  it("says Ajout en cours… while the member is added", async () => {
    // Given
    const wrapper = mountBookshelfView(
      salonOwned,
      [leaToOthers, marc, hugo, elodie],
      hugo,
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    await chooseHugo(screen);

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Ajouter" }));

    // Then
    screen.getByRole("button", { name: "Ajout en cours…" });
  });

  it("says Libris did not answer the add in the reader's row, the choice open", async () => {
    // Given
    const wrapper = mountBookshelfView(
      salonOwned,
      [leaToOthers, marc, hugo],
      new TypeError("Failed to fetch"),
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    await chooseHugo(screen);

    // When
    await fireEvent.click(screen.getByRole("button", { name: "Ajouter" }));
    await flushPromises();

    // Then
    const rows = within(
      screen.getByRole("list", { name: "Choisir un lecteur" }),
    ).getAllByRole("listitem");
    expect(rows).toHaveLength(1);
    within(rows[0]!).getByText("Hugo");
    within(rows[0]!).getByText(
      "Erreur lors de l'ajout, veuillez réessayer plus tard.",
    );
    expect(
      within(screen.getByRole("list", { name: "Membres" }))
        .getAllByRole("listitem")
        .map(normalised),
    ).toEqual(["Vous · propriétaire", "Marc · invité"]);
  });

  it("forgets the members it added once another bookshelf opens", async () => {
    // Given
    const wrapper = mountBookshelfView(
      salonOwned,
      [leaToOthers, marc, hugo],
      hugo,
    );
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    await chooseHugo(screen);
    await fireEvent.click(screen.getByRole("button", { name: "Ajouter" }));
    await flushPromises();
    await wrapper.setProps({ id: bibliothequeDeLea.id });
    await flushPromises();

    // When
    await wrapper.setProps({ id: salon.id });
    await flushPromises();

    // Then
    expect(
      within(screen.getByRole("list", { name: "Membres" }))
        .getAllByRole("listitem")
        .map(normalised),
    ).toEqual(["Vous · propriétaire", "Hugo · invité", "Marc · invité"]);
  });

  it("closes the choice once another bookshelf opens", async () => {
    // Given
    const wrapper = mountBookshelfView(salonOwned, [leaToOthers, marc, hugo]);
    await flushPromises();
    const screen = within(wrapper.element as HTMLElement);
    await fireEvent.click(
      screen.getByRole("button", { name: "Ajouter un membre" }),
    );
    await wrapper.setProps({ id: bibliothequeDeLea.id });
    await flushPromises();

    // When
    await wrapper.setProps({ id: salon.id });
    await flushPromises();

    // Then
    expect(
      screen.queryByRole("list", { name: "Choisir un lecteur" }),
    ).toBeNull();
  });

  it("offers a viewer no plus", async () => {
    // Given
    const wrapper = mountBookshelfView(salonHeld, [
      leaToOthers,
      marc,
      elodie,
      hugo,
    ]);

    // When
    await flushPromises();

    // Then
    const screen = within(wrapper.element as HTMLElement);
    expect(
      screen.queryByRole("button", { name: "Ajouter un membre" }),
    ).toBeNull();
  });

  it("says every reader is a member", async () => {
    // Given
    const wrapper = mountBookshelfView(salonOwned, [leaToOthers, marc]);

    // When
    await flushPromises();

    // Then
    within(wrapper.element as HTMLElement).getByText(
      "Tous les lecteurs sont déjà membres.",
    );
  });

  it("says nothing of it while a reader can be added", async () => {
    // Given
    const wrapper = mountBookshelfView(salonOwned, [leaToOthers, marc, hugo]);

    // When
    await flushPromises();

    // Then
    expect(
      within(wrapper.element as HTMLElement).queryByText(
        "Tous les lecteurs sont déjà membres.",
      ),
    ).toBeNull();
  });

  it("says nothing of it to a viewer", async () => {
    // Given
    const wrapper = mountBookshelfView(salonHeld, []);

    // When
    await flushPromises();

    // Then
    expect(
      within(wrapper.element as HTMLElement).queryByText(
        "Tous les lecteurs sont déjà membres.",
      ),
    ).toBeNull();
  });

  async function chooseHugo(screen: Screen) {
    await fireEvent.click(
      screen.getByRole("button", { name: "Ajouter un membre" }),
    );
    await fireEvent.click(
      within(
        screen.getByRole("list", { name: "Choisir un lecteur" }),
      ).getByRole("button", { name: "Hugo" }),
    );
  }

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

type Screen = BoundFunctions<typeof queries>;

function owner(reader: KnownReader): Member {
  return { reader, role: "OWNER" };
}

function viewer(reader: KnownReader): Member {
  return { reader, role: "VIEWER" };
}
