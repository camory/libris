import { BookOpen } from "@lucide/vue";
import { within } from "@testing-library/dom";
import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import RowCover from "./RowCover.vue";

describe("RowCover", () => {
  it("shows its picture, named after its alt", () => {
    // When
    const { screen } = show(romanceDawnCover, "Couverture de Romance dawn");

    // Then
    const picture = screen.getByRole("img", {
      name: "Couverture de Romance dawn",
    });
    expect(picture.getAttribute("src")).toBe(romanceDawnCover);
  });

  it("shows a book icon when it has no picture", () => {
    // When
    const { wrapper, screen } = show(null, "Couverture du Petit Prince");

    // Then
    expect(screen.queryByRole("img")).toBeNull();
    expect(wrapper.findAllComponents(BookOpen)).toHaveLength(1);
  });
});

function show(url: string | null, alt: string) {
  const wrapper = mount(RowCover, { props: { url, alt } });
  return { wrapper, screen: within(wrapper.element as HTMLElement) };
}

const romanceDawnCover =
  "/api/v1/covers/51aa137dfc73ec01925c94b5951649898ac8e50e911a392afb7af90baae87535";
