import { CircleAlert } from "@lucide/vue";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import {
  seriesApiKey,
  type FindSeriesAnswer,
  type SeriesApi,
} from "../../../application/SeriesApi";
import type { Series } from "../../../domain/Series";
import { FakeSeriesApi } from "../../../fixture/FakeSeriesApi";
import { normalised } from "../../../fixture/normalised";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
import FieldRowSkeleton from "../../components/FieldRowSkeleton.vue";
import PageHintSkeleton from "../../components/PageHintSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";
import { createLibrisI18n } from "../../i18n";
import SeriesPageView from "./SeriesPageView.vue";

const asterixId = "7c8d9e0f-1a2b-4c3d-8e4f-5a6b7c8d9e0f";
const tintinId = "3e4f5a6b-7c8d-4e9f-8a0b-1c2d3e4f5a6b";

const noSuchSeries: FindSeriesAnswer = {
  outcome: "problem",
  type: "/problems/not-found",
};

const mountSeriesPageView = (
  found: FindSeriesAnswer | SeriesApi,
  id = asterixId,
) =>
  mount(SeriesPageView, {
    props: { id },
    global: {
      plugins: [createLibrisI18n()],
      provide: {
        [seriesApiKey]:
          "outcome" in found ? new FakeSeriesApi([], found) : found,
      },
    },
  });

class LibrisRecordingAsks implements SeriesApi {
  readonly asked: string[] = [];

  owned(): Promise<Series[]> {
    return Promise.resolve([]);
  }

  find(id: string): Promise<FindSeriesAnswer> {
    this.asked.push(id);
    return Promise.resolve(noSuchSeries);
  }
}

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

  it("says there is no such series, alone", async () => {
    // Given
    const wrapper = mountSeriesPageView(noSuchSeries);

    // When
    await flushPromises();

    // Then
    expect(normalised(wrapper.element)).toBe("Série introuvable.");
    expect(wrapper.findAllComponents(CircleAlert)).toHaveLength(1);
  });

  it("asks the series of each id it is given", async () => {
    // Given
    const libris = new LibrisRecordingAsks();
    const wrapper = mountSeriesPageView(libris, asterixId);
    await flushPromises();

    // When
    await wrapper.setProps({ id: tintinId });
    await flushPromises();

    // Then
    expect(libris.asked).toEqual([asterixId, tintinId]);
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
