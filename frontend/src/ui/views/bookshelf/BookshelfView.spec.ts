import { within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import {
  bookshelvesApiKey,
  type FindAnswer,
} from "../../../application/BookshelvesApi";
import { meApiKey } from "../../../application/MeApi";
import type { KnownReader } from "../../../domain/KnownReader";
import type { Member } from "../../../domain/Member";
import { salon } from "../../../fixture/Books";
import { FakeBookshelvesApi } from "../../../fixture/FakeBookshelvesApi";
import { FakeMeApi } from "../../../fixture/FakeMeApi";
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

const salonHeld: FindAnswer = {
  outcome: "found",
  bookshelf: salon,
  members: [viewer(leaToOthers), owner(marc), owner(elodie)],
};

const mountBookshelfView = (find: FindAnswer | Error) =>
  mount(BookshelfView, {
    props: { id: salon.id },
    global: {
      plugins: [createLibrisI18n()],
      provide: {
        [meApiKey]: new FakeMeApi(lea),
        [bookshelvesApiKey]: new FakeBookshelvesApi({ memberships: [], find }),
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
