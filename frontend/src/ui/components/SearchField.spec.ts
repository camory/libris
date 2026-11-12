import { fireEvent, within } from "@testing-library/dom";
import { Search } from "@lucide/vue";
import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vitest";
import { createLibrisI18n } from "../i18n";
import SearchField from "./SearchField.vue";

describe("SearchField", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it("is a search box named for the catalogue", () => {
    // When
    const screen = within(field().element);

    // Then
    expect(
      screen.getAllByRole("searchbox", {
        name: "Rechercher dans le catalogue",
      }),
    ).toHaveLength(1);
  });

  it("shows an example of the words", () => {
    // When
    const screen = within(field().element);

    // Then
    expect(screen.getByRole("searchbox").getAttribute("placeholder")).toBe(
      "Titre, série, auteur, ISBN",
    );
  });

  it("shows the search glyph", () => {
    // When
    const wrapper = field();

    // Then
    expect(wrapper.findComponent(Search).exists()).toBe(true);
  });

  it("hands on the text as typed", async () => {
    // Given
    const wrapper = field();

    // When
    await fireEvent.input(within(wrapper.element).getByRole("searchbox"), {
      target: { value: " goscinny " },
    });

    // Then
    expect(wrapper.emitted("update:modelValue")).toEqual([[" goscinny "]]);
  });

  it("asks for the words once the reader pauses", async () => {
    // Given
    vi.useFakeTimers({ toFake: ["setTimeout", "clearTimeout"] });
    const wrapper = field();

    // When
    await type(wrapper, "goscinny");
    vi.advanceTimersByTime(299);

    // Then
    expect(wrapper.emitted("search")).toBeUndefined();

    // When
    vi.advanceTimersByTime(1);

    // Then
    expect(wrapper.emitted("search")).toEqual([["goscinny"]]);
  });
});

function field() {
  return mount(SearchField, {
    props: { modelValue: "" },
    global: { plugins: [createLibrisI18n()] },
  });
}

async function type(wrapper: ReturnType<typeof field>, text: string) {
  await fireEvent.input(within(wrapper.element).getByRole("searchbox"), {
    target: { value: text },
  });
}
