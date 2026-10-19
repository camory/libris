import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import {
  seriesApiKey,
  type FindSeriesAnswer,
} from "../../../application/SeriesApi";
import { FakeSeriesApi } from "../../../fixture/FakeSeriesApi";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
import FieldRowSkeleton from "../../components/FieldRowSkeleton.vue";
import PageHintSkeleton from "../../components/PageHintSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";
import { createLibrisI18n } from "../../i18n";
import SeriesPageView from "./SeriesPageView.vue";

const asterixId = "7c8d9e0f-1a2b-4c3d-8e4f-5a6b7c8d9e0f";

const noSuchSeries: FindSeriesAnswer = {
  outcome: "problem",
  type: "/problems/not-found",
};

const mountSeriesPageView = (found: FindSeriesAnswer, id = asterixId) =>
  mount(SeriesPageView, {
    props: { id },
    global: {
      plugins: [createLibrisI18n()],
      provide: { [seriesApiKey]: new FakeSeriesApi([], found) },
    },
  });

describe("SeriesPageView", () => {
  it("shows skeletons while the series comes", async () => {
    // Given
    const wrapper = mountSeriesPageView(noSuchSeries);
    const before = seen(wrapper);

    // When
    await flushPromises();

    // Then
    expect([before, seen(wrapper)]).toEqual([
      { title: 1, hint: 1, fields: 2, rows: 5 },
      { title: 0, hint: 0, fields: 0, rows: 0 },
    ]);
  });

  function seen(wrapper: ReturnType<typeof mountSeriesPageView>) {
    return {
      title: wrapper.findAllComponents(PageTitleSkeleton).length,
      hint: wrapper.findAllComponents(PageHintSkeleton).length,
      fields: wrapper.findAllComponents(FieldRowSkeleton).length,
      rows: wrapper.findAllComponents(CatalogueRowSkeleton).length,
    };
  }
});
