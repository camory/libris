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

  it("names a viewer over invité", () => {
    // When
    const row = show({ reader: marc, role: "VIEWER" }, false);

    // Then
    expect(normalised(row)).toBe("Marc · invité");
  });

  it("names the reader Vous", () => {
    // When
    const row = show({ reader: lea, role: "VIEWER" }, true);

    // Then
    expect(normalised(row)).toBe("Vous · invité");
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

const marc: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000005",
  displayName: "Marc",
};
