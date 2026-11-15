import { CircleAlert } from "@lucide/vue";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import FailureMessage from "./FailureMessage.vue";

describe("FailureMessage", () => {
  it("says its sentence", () => {
    // When
    const wrapper = show("ISBN invalide");

    // Then
    expect(wrapper.text()).toBe("ISBN invalide");
  });

  it("draws the alert beside its sentence", () => {
    // When
    const wrapper = show("ISBN inconnu");

    // Then
    expect(wrapper.findAllComponents(CircleAlert)).toHaveLength(1);
  });
});

function show(sentence: string) {
  return mount(FailureMessage, { props: { sentence } });
}
