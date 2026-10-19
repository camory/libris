import { BookOpen } from "@lucide/vue";
import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it } from "vitest";
import { nextTick } from "vue";
import type { Series } from "../../domain/Series";
import { createLibrisI18n } from "../i18n";
import { createLibrisRouter } from "../router";
import SeriesRow from "./SeriesRow.vue";

describe("SeriesRow", () => {
  afterEach(() => {
    window.history.replaceState(null, "", "/");
  });

  it("shows the name", async () => {
    // When
    const { screen } = await show(asterix);

    // Then
    expect(screen.getByText("Astérix")).toBeDefined();
  });

  it("shows the cover, named after the series", async () => {
    // When
    const { screen } = await show(asterix);

    // Then
    const cover = screen.getByRole("img", { name: "Couverture de Astérix" });
    expect(cover.getAttribute("src")).toBe(asterix.coverUrl);
  });

  it("shows a book icon when the series has no cover", async () => {
    // When
    const { wrapper, screen } = await show({ ...asterix, coverUrl: null });

    // Then
    expect(screen.queryByRole("img")).toBeNull();
    expect(wrapper.findAllComponents(BookOpen)).toHaveLength(1);
  });

  it("shows a book icon when the cover does not load", async () => {
    // Given
    const { wrapper, screen } = await show(asterix);

    // When
    screen.getByRole("img").dispatchEvent(new Event("error"));
    await nextTick();

    // Then
    expect(screen.queryByRole("img")).toBeNull();
    expect(wrapper.findAllComponents(BookOpen)).toHaveLength(1);
  });

  it("counts the albums of a BD series", async () => {
    // When
    const { screen } = await show({ ...asterix, kind: "BD", volumeCount: 6 });

    // Then
    expect(screen.getByText("6 albums")).toBeDefined();
  });

  it("counts one album", async () => {
    // When
    const { screen } = await show({ ...asterix, kind: "BD", volumeCount: 1 });

    // Then
    expect(screen.getByText("1 album")).toBeDefined();
  });

  it("counts the tomes of a manga series", async () => {
    // When
    const { screen } = await show({
      ...asterix,
      kind: "MANGA",
      volumeCount: 3,
    });

    // Then
    expect(screen.getByText("3 tomes")).toBeDefined();
  });

  it("counts one tome", async () => {
    // When
    const { screen } = await show({
      ...asterix,
      kind: "MANGA",
      volumeCount: 1,
    });

    // Then
    expect(screen.getByText("1 tome")).toBeDefined();
  });

  it("counts in tomes for a book series", async () => {
    // When
    const { screen } = await show({ ...asterix, kind: "BOOK", volumeCount: 3 });

    // Then
    expect(screen.getByText("3 tomes")).toBeDefined();
  });

  it("counts in tomes when the editions differ in kind", async () => {
    // When
    const { screen } = await show({ ...asterix, kind: null, volumeCount: 3 });

    // Then
    expect(screen.getByText("3 tomes")).toBeDefined();
  });

  it("shows the name and the count alone", async () => {
    // When
    const { wrapper } = await show(asterix);

    // Then
    expect(words(wrapper.element as HTMLElement)).toEqual([
      "Astérix",
      "6 albums",
    ]);
  });

  it("leads to the series' page", async () => {
    // When
    const { screen } = await show(asterix);

    // Then
    expect(screen.getByRole("link").getAttribute("href")).toBe(
      `/catalogue/series/${asterix.id}`,
    );
  });

  async function show(series: Series) {
    const router = createLibrisRouter();
    await router.push("/catalogue/series");
    await router.isReady();
    const wrapper = mount(SeriesRow, {
      props: { series },
      global: { plugins: [createLibrisI18n(), router] },
    });
    return {
      wrapper,
      screen: within(wrapper.element.parentElement as HTMLElement),
    };
  }

  function words(row: HTMLElement) {
    const texts = document.createTreeWalker(row, NodeFilter.SHOW_TEXT);
    const words: string[] = [];
    while (texts.nextNode()) {
      const text = texts.currentNode.textContent?.trim();
      if (text) words.push(text);
    }
    return words;
  }
});

const asterix: Series = {
  id: "4d5e6f7a-8b9c-4d0e-9f1a-3b4c5d6e7f80",
  name: "Astérix",
  volumeCount: 6,
  coverUrl:
    "/api/v1/covers/63dacaf17234db7995b80673af35f7cebb45ba7c0b514c11ffd8b143fc05ec27",
  kind: "BD",
};
