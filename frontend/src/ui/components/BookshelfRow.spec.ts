import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { Membership } from "../../domain/Membership";
import { salon } from "../../fixture/Books";
import { createLibrisI18n } from "../i18n";
import BookshelfRow from "./BookshelfRow.vue";

describe("BookshelfRow", () => {
  it("names an owned bookshelf over propriétaire", () => {
    // When
    const row = show({ bookshelf: salon, role: "OWNER" }, false);

    // Then
    expect(normalised(row)).toBe("Salon propriétaire");
  });

  function show(membership: Membership, isDefault: boolean) {
    return mount(BookshelfRow, {
      props: { membership, isDefault },
      global: { plugins: [createLibrisI18n()] },
    }).element;
  }

  function normalised(element: Element) {
    return element.textContent?.replace(/\s+/g, " ").trim() ?? "";
  }
});
