import { fireEvent, within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createLibrisI18n } from "../i18n";
import AppUpdateBanner, { type UpdateState } from "./AppUpdateBanner.vue";
import BusySpinner from "./BusySpinner.vue";
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

  it("gives the order on the tap", async () => {
    // Given
    const { wrapper, screen } = open("ready");

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "Mettre à jour" }),
    );

    // Then
    expect(wrapper.emitted("update")).toHaveLength(1);
  });

  it("says the update is running and takes no second order", () => {
    // When
    const { wrapper, screen } = open("updating");

    // Then
    screen.getByText("Mise à jour…");
    expect(wrapper.findComponent(BusySpinner).exists()).toBe(true);
    expect(screen.queryByRole("button")).toBeNull();
    expect(wrapper.findComponent(IconRefresh).exists()).toBe(false);
  });

  it("shows nothing while no version waits", () => {
    // When
    const { wrapper } = open("none");

    // Then
    expect(wrapper.text()).toBe("");
    expect(wrapper.find("*").exists()).toBe(false);
  });

  function open(state: UpdateState) {
    const wrapper = mount(AppUpdateBanner, {
      global: { plugins: [createLibrisI18n()] },
      props: { state },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }
});
