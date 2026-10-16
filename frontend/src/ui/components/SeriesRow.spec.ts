import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { Series } from "../../domain/Series";
import { createLibrisI18n } from "../i18n";
import SeriesRow from "./SeriesRow.vue";

describe("SeriesRow", () => {
  it("shows the name", () => {
    // When
    const { screen } = show(asterix);

    // Then
    expect(screen.getByText("Astérix")).toBeDefined();
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
