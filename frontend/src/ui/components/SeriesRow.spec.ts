import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { nextTick } from "vue";
import type { Series } from "../../domain/Series";
import { createLibrisI18n } from "../i18n";
import IconBook from "./icons/IconBook.vue";
import SeriesRow from "./SeriesRow.vue";

describe("SeriesRow", () => {
  it("shows the name", () => {
    // When
    const { screen } = show(asterix);

    // Then
    expect(screen.getByText("Astérix")).toBeDefined();
  });

  it("shows the cover, named after the series", () => {
    // When
    const { screen } = show(asterix);

    // Then
    const cover = screen.getByRole("img", { name: "Couverture de Astérix" });
    expect(cover.getAttribute("src")).toBe(asterix.coverUrl);
  });

  it("shows a book icon when the series has no cover", () => {
    // When
    const { wrapper, screen } = show({ ...asterix, coverUrl: null });

    // Then
    expect(screen.queryByRole("img")).toBeNull();
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(1);
  });

  it("shows a book icon when the cover does not load", async () => {
    // Given
    const { wrapper, screen } = show(asterix);

    // When
    screen.getByRole("img").dispatchEvent(new Event("error"));
    await nextTick();

    // Then
    expect(screen.queryByRole("img")).toBeNull();
    expect(wrapper.findAllComponents(IconBook)).toHaveLength(1);
  });

  it("counts the albums of a BD series", () => {
    // When
    const { screen } = show({ ...asterix, kind: "BD", volumeCount: 6 });

    // Then
    expect(screen.getByText("6 albums")).toBeDefined();
  });

  it("counts one album", () => {
    // When
    const { screen } = show({ ...asterix, kind: "BD", volumeCount: 1 });

    // Then
    expect(screen.getByText("1 album")).toBeDefined();
  });

  it("counts the tomes of a manga series", () => {
    // When
    const { screen } = show({ ...asterix, kind: "MANGA", volumeCount: 3 });

    // Then
    expect(screen.getByText("3 tomes")).toBeDefined();
  });

  it("counts one tome", () => {
    // When
    const { screen } = show({ ...asterix, kind: "MANGA", volumeCount: 1 });

    // Then
    expect(screen.getByText("1 tome")).toBeDefined();
  });

  it("counts in tomes for a book series", () => {
    // When
    const { screen } = show({ ...asterix, kind: "BOOK", volumeCount: 3 });

    // Then
    expect(screen.getByText("3 tomes")).toBeDefined();
  });

  function show(series: Series) {
    const wrapper = mount(SeriesRow, {
      props: { series },
      global: { plugins: [createLibrisI18n()] },
    });
    return { wrapper, screen: within(wrapper.element as HTMLElement) };
  }
});

const asterix: Series = {
  name: "Astérix",
  volumeCount: 6,
  coverUrl:
    "/api/v1/covers/63dacaf17234db7995b80673af35f7cebb45ba7c0b514c11ffd8b143fc05ec27",
  kind: "BD",
};
