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

  it("offers Ouvrages then Séries, each a link to its view", async () => {
    // When
    const screen = await show("/catalogue");

    // Then
    expect(
      screen
        .getAllByRole("link")
        .map((link) => [link.textContent?.trim(), link.getAttribute("href")]),
    ).toEqual([
      ["Ouvrages", "/catalogue"],
      ["Séries", "/catalogue/series"],
    ]);
  });

  it("marks Ouvrages current on the ouvrages", async () => {
    // When
    const screen = await show("/catalogue");

    // Then
    expect(
      screen.getByRole("link", { current: "page" }).textContent?.trim(),
    ).toBe("Ouvrages");
  });

  it("marks Séries alone current on the series", async () => {
    // When
    const screen = await show("/catalogue/series");

    // Then
    expect(
      screen
        .getAllByRole("link", { current: "page" })
        .map((link) => link.textContent?.trim()),
    ).toEqual(["Séries"]);
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
