import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import {
  catalogueApiKey,
  type CatalogueApi,
} from "../../../application/CatalogueApi";
import { FakeCatalogueApi } from "../../../fixture/FakeCatalogueApi";
import { createLibrisI18n } from "../../i18n";
import CatalogueView from "./CatalogueView.vue";

describe("CatalogueView", () => {
  it("shows the title and the hint", () => {
    // When
    const { screen } = open(new FakeCatalogueApi([{ books: [], next: null }]));

    // Then
    expect(
      screen.getByRole("heading", { name: "Parcourir le catalogue" }),
    ).toBeDefined();
    expect(
      screen.getByText("Les ouvrages de toutes vos bibliothèques."),
    ).toBeDefined();
  });

  function open(catalogueApi: CatalogueApi) {
    const wrapper = mount(CatalogueView, {
      global: {
        plugins: [createLibrisI18n()],
        provide: { [catalogueApiKey]: catalogueApi },
      },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }
});
