import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { i18n } from "../../i18n";
import HomeView from "./HomeView.vue";

describe("HomeView", () => {
  it("renders the French title from the messages", () => {
    const wrapper = mount(HomeView, { global: { plugins: [i18n] } });

    expect(wrapper.get("h1").text()).toBe("La bibliothèque de la maison");
  });
});
