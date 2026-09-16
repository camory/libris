import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it } from "vitest";
import { createLibrisI18n } from "../i18n";
import { createLibrisRouter } from "../router";
import AppTabBar from "./AppTabBar.vue";

describe("AppTabBar", () => {
  afterEach(() => {
    window.history.replaceState(null, "", "/");
  });

  it("shows one tab per top-level screen, in order", async () => {
    const screen = await open("/");

    const tabs = screen.getAllByRole("link");
    expect(tabs).toHaveLength(2);
    expect(tabs[0]).toBe(screen.getByRole("link", { name: "Accueil" }));
    expect(tabs[1]).toBe(screen.getByRole("link", { name: "Ajouter" }));
    expect(tabs.map((tab) => tab.getAttribute("href"))).toEqual(["/", "/isbn"]);
  });

  async function open(path: string) {
    const router = createLibrisRouter();
    await router.push(path);
    await router.isReady();
    const wrapper = mount(AppTabBar, {
      global: { plugins: [createLibrisI18n(), router] },
    });
    return within(wrapper.element as HTMLElement);
  }
});
