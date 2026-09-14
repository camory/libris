import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { createLibrisI18n } from "../../i18n";
import IsbnView from "./IsbnView.vue";

const mountIsbnView = () =>
  mount(IsbnView, { global: { plugins: [createLibrisI18n()] } });

describe("IsbnView", () => {
  it("renders the title and the hint", () => {
    const wrapper = mountIsbnView();

    expect(wrapper.text()).toContain("Ajouter un ouvrage");
    expect(wrapper.text()).toContain(
      "Scannez le code-barres ou saisissez l'ISBN.",
    );
  });
});
