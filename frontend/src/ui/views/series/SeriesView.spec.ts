import { within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { seriesApiKey, type SeriesApi } from "../../../application/SeriesApi";
import type { Series } from "../../../domain/Series";
import { FakeSeriesApi } from "../../../fixture/FakeSeriesApi";
import SeriesRowSkeleton from "../../components/SeriesRowSkeleton.vue";
import IconAlert from "../../components/icons/IconAlert.vue";
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

  it("asks the series on each arrival", async () => {
    // Given
    let asks = 0;
    const seriesApi: SeriesApi = {
      owned: () => {
        asks += 1;
        return Promise.resolve([]);
      },
    };
    open(seriesApi).wrapper.unmount();
    await flushPromises();

    // When
    open(seriesApi);
    await flushPromises();

    // Then
    expect(asks).toBe(2);
  });

  it("lists one row per series Libris answers", async () => {
    // Given
    const { screen } = open(new FakeSeriesApi([onePiece, asterix]));

    // When
    await flushPromises();

    // Then
    expect(screen.getAllByRole("listitem")).toHaveLength(2);
  });

  it("lists the series by name whatever the order of the answer", async () => {
    // Given
    const { screen } = open(new FakeSeriesApi([onePiece, asterix]));

    // When
    await flushPromises();

    // Then
    const [first, second] = screen.getAllByRole("listitem");
    expect(within(first!).getByText("Astérix")).toBeDefined();
    expect(within(second!).getByText("One piece")).toBeDefined();
  });

  it("says to try again later when the series do not come", async () => {
    // Given
    const { wrapper, screen } = open(
      new FakeSeriesApi(new TypeError("Failed to fetch")),
    );

    // When
    await flushPromises();

    // Then
    expect(
      screen.getByText(
        "Erreur lors du chargement, veuillez réessayer plus tard.",
      ),
    ).toBeDefined();
    expect(wrapper.findAllComponents(IconAlert)).toHaveLength(1);
  });

  it("lists nothing when the series do not come", async () => {
    // Given
    const { wrapper, screen } = open(
      new FakeSeriesApi(new TypeError("Failed to fetch")),
    );

    // When
    await flushPromises();

    // Then
    expect(wrapper.findAllComponents(SeriesRowSkeleton)).toHaveLength(0);
    expect(screen.queryAllByRole("listitem")).toEqual([]);
    expect(screen.queryByRole("list")).toBeNull();
    expect(
      screen.queryByText("Les séries de vos bibliothèques apparaîtront ici."),
    ).toBeNull();
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

const onePiece: Series = {
  name: "One piece",
  volumeCount: 3,
  coverUrl: null,
  kind: "MANGA",
};

const asterix: Series = {
  name: "Astérix",
  volumeCount: 6,
  coverUrl: null,
  kind: "BD",
};
