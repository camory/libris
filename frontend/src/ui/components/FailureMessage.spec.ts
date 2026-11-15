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
});

function show(sentence: string) {
  return mount(FailureMessage, { props: { sentence } });
}
