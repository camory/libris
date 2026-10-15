import { within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { seriesApiKey, type SeriesApi } from "../../../application/SeriesApi";
import { FakeSeriesApi } from "../../../fixture/FakeSeriesApi";
import SeriesRowSkeleton from "../../components/SeriesRowSkeleton.vue";
import IconBook from "../../components/icons/IconBook.vue";
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

  it("shows no skeleton row once the series have come", async () => {
    // Given
    const { wrapper } = open(new FakeSeriesApi([]));

    // When
    await flushPromises();

    // Then
    expect(wrapper.findAllComponents(SeriesRowSkeleton)).toHaveLength(0);
  });

  it("says what will fill the series when there are none", async () => {
    // Given
    const { wrapper, screen } = open(new FakeSeriesApi([]));

    // When
    await flushPromises();

    // Then
    expect(
      screen.getByText("Les séries de vos bibliothèques apparaîtront ici."),
    ).toBeDefined();
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(1);
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
