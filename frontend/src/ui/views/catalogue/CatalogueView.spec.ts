import { within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import {
  catalogueApiKey,
  type CatalogueApi,
} from "../../../application/CatalogueApi";
import { FakeCatalogueApi } from "../../../fixture/FakeCatalogueApi";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
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

  it("shows five skeleton rows until the first page comes", async () => {
    // When
    const { wrapper } = open(new FakeCatalogueApi([{ books: [], next: null }]));

    // Then
    expect(wrapper.findAllComponents(CatalogueRowSkeleton)).toHaveLength(5);
    await flushPromises();
    expect(wrapper.findAllComponents(CatalogueRowSkeleton)).toHaveLength(0);
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
