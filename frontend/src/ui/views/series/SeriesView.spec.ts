import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { seriesApiKey, type SeriesApi } from "../../../application/SeriesApi";
import { FakeSeriesApi } from "../../../fixture/FakeSeriesApi";
import SeriesRowSkeleton from "../../components/SeriesRowSkeleton.vue";
import { createLibrisI18n } from "../../i18n";
import SeriesView from "./SeriesView.vue";

describe("SeriesView", () => {
  it("shows the catalogue's title and hint", () => {
    // When
    const { screen } = open(new FakeSeriesApi([]));

    // Then
    expect(
      screen.getByRole("heading", { name: "Parcourir le catalogue" }),
    ).toBeDefined();
    expect(
      screen.getByText("Les ouvrages de toutes vos bibliothèques."),
    ).toBeDefined();
  });

  it("shows five skeleton rows while the series are coming", () => {
    // When
    const { wrapper } = open(new FakeSeriesApi([]));

    // Then
    expect(wrapper.findAllComponents(SeriesRowSkeleton)).toHaveLength(5);
  });

  function open(seriesApi: SeriesApi) {
    const wrapper = mount(SeriesView, {
      global: {
        plugins: [createLibrisI18n()],
        provide: { [seriesApiKey]: seriesApi },
      },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }
});
