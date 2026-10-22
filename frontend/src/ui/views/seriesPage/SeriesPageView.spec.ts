import { CircleAlert } from "@lucide/vue";
import { within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { nextTick } from "vue";
import {
  seriesApiKey,
  type FindSeriesAnswer,
  type SeriesApi,
} from "../../../application/SeriesApi";
import type { Series, SeriesWithBooks } from "../../../domain/Series";
import { asterixEtSesAmis, asterixLeGaulois } from "../../../fixture/Books";
import { FakeSeriesApi } from "../../../fixture/FakeSeriesApi";
import { normalised } from "../../../fixture/normalised";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
import FieldRowSkeleton from "../../components/FieldRowSkeleton.vue";
import PageHintSkeleton from "../../components/PageHintSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";
import { createLibrisI18n } from "../../i18n";
import { createLibrisRouter } from "../../router";
import SeriesPageView from "./SeriesPageView.vue";

const mountSeriesPageView = (
  found: FindSeriesAnswer | SeriesApi,
  id = asterixId,
) =>
  mount(SeriesPageView, {
    props: { id },
    global: {
      plugins: [createLibrisI18n(), createLibrisRouter()],
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

  it("says to try again later when Libris does not answer, alone", async () => {
    // Given
    const wrapper = mountSeriesPageView(
      new FakeSeriesApi([], new TypeError("Failed to fetch")),
    );

    // When
    await flushPromises();

    // Then
    expect(normalised(wrapper.element)).toBe(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
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

  it("shows skeletons again while another series comes", async () => {
    // Given
    const wrapper = mountSeriesPageView(noSuchSeries, asterixId);
    await flushPromises();

    // When
    await wrapper.setProps({ id: tintinId });
    const during = seen(wrapper);
    await flushPromises();

    // Then
    expect(during).toEqual({ title: 1, hint: 1, fields: 2, rows: 5 });
    expect(normalised(wrapper.element)).toBe("Série introuvable.");
  });

  it("asks the series on each arrival", async () => {
    // Given
    const libris = new LibrisRecordingAsks();
    mountSeriesPageView(libris, asterixId).unmount();
    await flushPromises();

    // When
    mountSeriesPageView(libris, asterixId);
    await flushPromises();

    // Then
    expect(libris.asked).toEqual([asterixId, asterixId]);
  });

  it("titles the page with the series' name", async () => {
    // Given
    const wrapper = mountSeriesPageView(asterixFound);

    // When
    await flushPromises();

    // Then
    expect(
      within(wrapper.element as HTMLElement)
        .getAllByRole("heading")
        .map(({ textContent }) => textContent),
    ).toEqual(["Astérix"]);
  });

  it("opens with the series' cover above its name", async () => {
    // Given
    const wrapper = mountSeriesPageView({
      outcome: "found",
      series: { ...asterix, coverUrl: asterixCover },
    });

    // When
    await flushPromises();

    // Then
    const bands = bandsOf(wrapper, "Astérix");
    expect(bands.map((band) => band.getAttribute("src"))).toEqual([
      asterixCover,
    ]);
    expect(
      bands[0]?.compareDocumentPosition(
        within(wrapper.element as HTMLElement).getByRole("heading", {
          name: "Astérix",
        }),
      ),
    ).toBe(Node.DOCUMENT_POSITION_FOLLOWING);
  });

  it("draws no band for a series without a cover", async () => {
    // Given
    const wrapper = mountSeriesPageView(asterixFound);

    // When
    await flushPromises();

    // Then
    expect(bandsOf(wrapper, "Astérix")).toEqual([]);
  });

  it("draws no band once its cover fails to load", async () => {
    // Given
    const wrapper = mountSeriesPageView({
      outcome: "found",
      series: { ...asterix, coverUrl: asterixCover },
    });
    await flushPromises();

    // When
    bandsOf(wrapper, "Astérix")[0]?.dispatchEvent(new Event("error"));
    await nextTick();

    // Then
    expect(bandsOf(wrapper, "Astérix")).toEqual([]);
    expect(
      within(wrapper.element as HTMLElement).getByRole("heading", {
        name: "Astérix",
      }),
    ).toBeDefined();
  });

  it("shows the band of a series that comes after one whose cover failed", async () => {
    // Given
    const wrapper = mountSeriesPageView(
      new LibrisAnsweringById([
        { ...asterix, coverUrl: asterixCover },
        { ...asterix, id: tintinId, name: "Tintin", coverUrl: tintinCover },
      ]),
      asterixId,
    );
    await flushPromises();
    bandsOf(wrapper, "Astérix")[0]?.dispatchEvent(new Event("error"));
    await nextTick();

    // When
    await wrapper.setProps({ id: tintinId });
    await flushPromises();

    // Then
    expect(
      bandsOf(wrapper, "Tintin").map((band) => band.getAttribute("src")),
    ).toEqual([tintinCover]);
  });

  it("says how many albums the reader's bookshelves hold", async () => {
    // Given
    const wrapper = mountSeriesPageView(asterixFound);

    // When
    await flushPromises();

    // Then
    expect(
      within(wrapper.element as HTMLElement).getByText(
        "2 albums dans vos bibliothèques.",
      ),
    ).toBeDefined();
  });

  it("lists the editions in the order a shelf reads", async () => {
    // Given
    const wrapper = mountSeriesPageView(asterixFound);

    // When
    await flushPromises();

    // Then
    expect(
      within(wrapper.element as HTMLElement)
        .queryAllByRole("listitem")
        .map(normalised),
    ).toEqual([
      "Album 1 Astérix le Gaulois René Goscinny, Albert Uderzo Bibliothèque de Léa",
      "Astérix et ses amis René Goscinny, Albert Uderzo Salon",
    ]);
  });

  it("shows the albums owned between the header and the editions", async () => {
    // Given
    const wrapper = mountSeriesPageView(asterixFound);

    // When
    await flushPromises();

    // Then
    const text = normalised(wrapper.element);
    const positions = [
      "2 albums dans vos bibliothèques.",
      "Albums possédés",
      "Astérix le Gaulois",
    ].map((part) => text.indexOf(part));
    expect(positions).not.toContain(-1);
    expect(positions).toEqual([...positions].sort((a, b) => a - b));
  });

  it("lists a row for a missing album in its place", async () => {
    // Given
    const wrapper = mountSeriesPageView(asterixWithAGap);

    // When
    await flushPromises();

    // Then
    expect(
      within(wrapper.element as HTMLElement)
        .queryAllByRole("listitem")
        .map(normalised),
    ).toEqual([
      "Album 1 Astérix le Gaulois René Goscinny, Albert Uderzo Bibliothèque de Léa",
      "Album 2 Manquant",
      "Album 3 Astérix et les Goths René Goscinny, Albert Uderzo Bibliothèque de Léa",
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

  function bandsOf(
    wrapper: ReturnType<typeof mountSeriesPageView>,
    name: string,
  ) {
    return within(wrapper.element as HTMLElement).queryAllByRole("img", {
      name: `Couverture de ${name}`,
    });
  }
});

class LibrisAnsweringById implements SeriesApi {
  constructor(private readonly series: SeriesWithBooks[]) {}

  owned(): Promise<Series[]> {
    return Promise.resolve([]);
  }

  find(id: string): Promise<FindSeriesAnswer> {
    const found = this.series.find((each) => each.id === id);
    return Promise.resolve(
      found ? { outcome: "found", series: found } : noSuchSeries,
    );
  }
}

const asterixId = "7c8d9e0f-1a2b-4c3d-8e4f-5a6b7c8d9e0f";
const tintinId = "3e4f5a6b-7c8d-4e9f-8a0b-1c2d3e4f5a6b";

const asterix: SeriesWithBooks = {
  id: asterixId,
  name: "Astérix",
  volumeCount: 2,
  coverUrl: null,
  kind: "BD",
  books: [asterixEtSesAmis, asterixLeGaulois],
};

const asterixCover =
  "/api/v1/covers/5e2b7f0c9d1a4e3b8c6f2a7d0e9b1c4f5a8d3e6b2c7f0a9d4e1b8c5f2a7d0e9b";

const tintinCover =
  "/api/v1/covers/9a4c1e7b3d0f6a2c8e5b1d4f7a0c3e6b9d2f5a8c1e4b7d0a3f6c9e2b5d8a1c4f";

const asterixFound: FindSeriesAnswer = { outcome: "found", series: asterix };

const asterixWithAGap: FindSeriesAnswer = {
  outcome: "found",
  series: {
    ...asterix,
    books: [
      {
        ...asterixLeGaulois,
        id: "8d9e0f1a-2b3c-4d4e-9f5a-6b7c8d9e0f1a",
        title: "Astérix et les Goths",
        series: { name: "Astérix", volumeNumber: 3 },
      },
      asterixLeGaulois,
    ],
  },
};

const noSuchSeries: FindSeriesAnswer = {
  outcome: "problem",
  type: "/problems/not-found",
};
