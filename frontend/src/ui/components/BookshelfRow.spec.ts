import { within } from "@testing-library/dom";
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

  async function show(membership: Membership, isDefault: boolean) {
    const router = createLibrisRouter();
    await router.push("/");
    await router.isReady();
    return mount(BookshelfRow, {
      props: { membership, isDefault },
      global: { plugins: [createLibrisI18n(), router] },
    }).element as HTMLElement;
  }
});
