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

  it("starts the pause again at each keystroke", async () => {
    // Given
    vi.useFakeTimers({ toFake: ["setTimeout", "clearTimeout"] });
    const wrapper = field();

    // When
    await type(wrapper, "go");
    vi.advanceTimersByTime(200);
    await type(wrapper, "gos");
    vi.advanceTimersByTime(200);

    // Then
    expect(wrapper.emitted("search")).toBeUndefined();

    // When
    vi.advanceTimersByTime(100);

    // Then
    expect(wrapper.emitted("search")).toEqual([["gos"]]);
  });

  it("asks at once on the search key", async () => {
    // Given
    vi.useFakeTimers({ toFake: ["setTimeout", "clearTimeout"] });
    const wrapper = field();
    await type(wrapper, "goscinny");

    // When
    await pressTheSearchKey(wrapper);

    // Then
    expect(wrapper.emitted("search")).toEqual([["goscinny"]]);
  });

  it("asks once when the search key ends the pause", async () => {
    // Given
    vi.useFakeTimers({ toFake: ["setTimeout", "clearTimeout"] });
    const wrapper = field();
    await type(wrapper, "goscinny");

    // When
    await pressTheSearchKey(wrapper);
    vi.advanceTimersByTime(300);

    // Then
    expect(wrapper.emitted("search")).toEqual([["goscinny"]]);
  });

  it("closes the keyboard on the search key", async () => {
    // Given
    const wrapper = field({ attachTo: document.body });
    box(wrapper).focus();

    // When
    await pressTheSearchKey(wrapper);

    // Then
    expect(document.activeElement).not.toBe(box(wrapper));
    wrapper.unmount();
  });

  it("offers the cross once the box holds text", async () => {
    // Given
    const wrapper = field();

    // When
    await type(wrapper, "goscinny");

    // Then
    expect(
      within(wrapper.element).getAllByRole("button", {
        name: "Effacer la recherche",
      }),
    ).toHaveLength(1);
  });
});

function field(options: { attachTo?: HTMLElement } = {}) {
  return mount(SearchField, {
    props: { modelValue: "" },
    global: { plugins: [createLibrisI18n()] },
    ...options,
  });
}

async function type(wrapper: ReturnType<typeof field>, text: string) {
  await fireEvent.input(box(wrapper), { target: { value: text } });
}

async function pressTheSearchKey(wrapper: ReturnType<typeof field>) {
  const form = box(wrapper).form;
  if (form === null) throw Error("The search box sits in no form");
  await fireEvent.submit(form);
}

function box(wrapper: ReturnType<typeof field>) {
  return within(wrapper.element).getByRole<HTMLInputElement>("searchbox");
}
