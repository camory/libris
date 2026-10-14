import { fireEvent, within } from "@testing-library/dom";
import { ChevronRight } from "@lucide/vue";
import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it } from "vitest";
import type { Membership } from "../../domain/Membership";
import { salon } from "../../fixture/Books";
import { normalised } from "../../fixture/normalised";
import { createLibrisI18n } from "../i18n";
import { createLibrisRouter } from "../router";
import BookshelfRow from "./BookshelfRow.vue";

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

  async function show(membership: Membership, isDefault: boolean) {
    return (await mountRow(membership, isDefault)).element as HTMLElement;
  }

  async function mountRow(membership: Membership, isDefault: boolean) {
    const router = createLibrisRouter();
    await router.push("/");
    await router.isReady();
    return mount(BookshelfRow, {
      props: { membership, isDefault },
      global: { plugins: [createLibrisI18n(), router] },
    });
  }
});
