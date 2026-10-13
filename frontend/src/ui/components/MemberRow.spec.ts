import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { KnownReader } from "../../domain/KnownReader";
import type { Member } from "../../domain/Member";
import { normalised } from "../../fixture/normalised";
import { createLibrisI18n } from "../i18n";
import MemberRow from "./MemberRow.vue";

describe("MemberRow", () => {
  it("names an owner over propriétaire", () => {
    // When
    const row = show({ reader: lea, role: "OWNER" }, false);

    // Then
    expect(normalised(row)).toBe("Léa · propriétaire");
  });

  function show(member: Member, isReader: boolean) {
    return mount(MemberRow, {
      props: { member, isReader },
      global: { plugins: [createLibrisI18n()] },
    }).element;
  }
});

const lea: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000002",
  displayName: "Léa",
};
