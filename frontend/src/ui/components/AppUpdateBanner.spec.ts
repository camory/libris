import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createLibrisI18n } from "../i18n";
import AppUpdateBanner from "./AppUpdateBanner.vue";
import IconRefresh from "./icons/IconRefresh.vue";

describe("AppUpdateBanner", () => {
  it("shows the new version and the way to install it", () => {
    // When
    const { wrapper, screen } = open("ready");

    // Then
    screen.getByText("Nouvelle version disponible");
    screen.getByRole("button", { name: "Mettre à jour" });
    expect(wrapper.findComponent(IconRefresh).exists()).toBe(true);
  });

  function open(state: "none" | "ready" | "updating") {
    const wrapper = mount(AppUpdateBanner, {
      global: { plugins: [createLibrisI18n()] },
      props: { state },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }
});
