import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it } from "vitest";
import { createLibrisI18n } from "../i18n";
import { createLibrisRouter } from "../router";
import CatalogueSwitch from "./CatalogueSwitch.vue";

describe("CatalogueSwitch", () => {
  afterEach(() => {
    window.history.replaceState(null, "", "/");
  });

  it("is a navigation named Vue du catalogue", async () => {
    // When
    const screen = await show("/catalogue");

    // Then
    expect(
      screen.getByRole("navigation", { name: "Vue du catalogue" }),
    ).toBeDefined();
  });

  async function show(path: string) {
    const router = createLibrisRouter();
    await router.push(path);
    await router.isReady();
    const wrapper = mount(CatalogueSwitch, {
      global: { plugins: [createLibrisI18n(), router] },
    });
    return within(wrapper.element.parentElement as HTMLElement);
  }
});
