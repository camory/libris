import { fireEvent, within } from "@testing-library/dom";
import { ChevronRight, Star } from "@lucide/vue";
import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it } from "vitest";
import type { Membership } from "../../domain/Membership";
import { salon } from "../../fixture/Books";
import { normalised } from "../../fixture/normalised";
import { createLibrisI18n } from "../i18n";
import { createLibrisRouter } from "../router";
import BookshelfRow from "./BookshelfRow.vue";
import BusySpinner from "./BusySpinner.vue";

describe("BookshelfRow", () => {
  afterEach(() => {
    window.history.replaceState(null, "", "/");
  });

  it("names an owned bookshelf over propriétaire", async () => {
    // When
    const row = await show({ bookshelf: salon, role: "OWNER" }, false);

    // Then
    expect(normalised(row)).toBe("Salon propriétaire");
  });

  it("names a bookshelf the reader views over invité", async () => {
    // When
    const row = await show({ bookshelf: salon, role: "VIEWER" }, false);

    // Then
    expect(normalised(row)).toBe("Salon invité");
  });

  it("marks the reader's default", async () => {
    // When
    const row = await show({ bookshelf: salon, role: "OWNER" }, true);

    // Then
    expect(normalised(row)).toBe("Salon propriétaire · par défaut");
  });

  it("leads to the bookshelf's screen", async () => {
    // When
    const row = await show({ bookshelf: salon, role: "OWNER" }, false);

    // Then
    expect(within(row).getByRole("link").getAttribute("href")).toBe(
      `/bibliotheques/${salon.id}`,
    );
  });

  it("ends with a chevron", async () => {
    // When
    const wrapper = await mountRow({ bookshelf: salon, role: "OWNER" }, false);

    // Then
    expect(wrapper.findComponent(ChevronRight).exists()).toBe(true);
  });

  it("offers to make an owned bookshelf the default", async () => {
    // When
    const row = await show({ bookshelf: salon, role: "OWNER" }, false);

    // Then
    expect(
      within(row).getByRole("button", {
        name: "Choisir comme bibliothèque par défaut",
      }),
    ).toBeDefined();
  });

  it("asks for the bookshelf on a tap of its star", async () => {
    // Given
    const wrapper = await mountRow({ bookshelf: salon, role: "OWNER" }, false);

    // When
    await fireEvent.click(
      within(wrapper.element as HTMLElement).getByRole("button", {
        name: "Choisir comme bibliothèque par défaut",
      }),
    );

    // Then
    expect(wrapper.emitted("choose")).toHaveLength(1);
  });

  it("marks the default with a star that is no button", async () => {
    // When
    const wrapper = await mountRow({ bookshelf: salon, role: "OWNER" }, true);

    // Then
    expect(
      within(wrapper.element as HTMLElement).queryByRole("button"),
    ).toBeNull();
    expect(wrapper.findComponent(Star).attributes("fill")).toBe("currentColor");
  });

  it("outlines the star of the button", async () => {
    // When
    const wrapper = await mountRow({ bookshelf: salon, role: "OWNER" }, false);

    // Then
    expect(wrapper.findComponent(Star).attributes("fill")).not.toBe(
      "currentColor",
    );
  });

  it("draws no star on a viewer's row", async () => {
    // When
    const wrapper = await mountRow({ bookshelf: salon, role: "VIEWER" }, false);

    // Then
    expect(wrapper.findAllComponents(Star)).toHaveLength(0);
    expect(
      within(wrapper.element as HTMLElement).queryByRole("button"),
    ).toBeNull();
  });

  it("is busy while its choice runs", async () => {
    // When
    const wrapper = await mountRow({ bookshelf: salon, role: "OWNER" }, false, {
      busy: true,
    });

    // Then
    const row = within(wrapper.element as HTMLElement);
    const busy = row.getByRole<HTMLButtonElement>("button", {
      name: "Changement en cours…",
    });
    expect(busy.disabled).toBe(true);
    expect(wrapper.findComponent(BusySpinner).exists()).toBe(true);
    expect(
      row.queryByRole("button", {
        name: "Choisir comme bibliothèque par défaut",
      }),
    ).toBeNull();
  });

  it("accepts nothing while a choice runs", async () => {
    // When
    const row = await show({ bookshelf: salon, role: "OWNER" }, false, {
      locked: true,
    });

    // Then
    expect(
      within(row).getByRole<HTMLButtonElement>("button", {
        name: "Choisir comme bibliothèque par défaut",
      }).disabled,
    ).toBe(true);
  });

  async function show(
    membership: Membership,
    isDefault: boolean,
    choice: Choice = {},
  ) {
    return (await mountRow(membership, isDefault, choice))
      .element as HTMLElement;
  }

  async function mountRow(
    membership: Membership,
    isDefault: boolean,
    choice: Choice = {},
  ) {
    const router = createLibrisRouter();
    await router.push("/");
    await router.isReady();
    return mount(BookshelfRow, {
      props: { membership, isDefault, ...choice },
      global: { plugins: [createLibrisI18n(), router] },
    });
  }
});

interface Choice {
  busy?: boolean;
  locked?: boolean;
}
