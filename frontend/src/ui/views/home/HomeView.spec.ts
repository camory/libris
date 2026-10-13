import { within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { meApiKey } from "../../../application/MeApi";
import { revisionKey } from "../../../application/Revision";
import { FakeMeApi } from "../../../fixture/FakeMeApi";
import type { Reader } from "../../../domain/Reader";
import { chloe } from "../../../fixture/Readers";
import { createLibrisI18n } from "../../i18n";
import { createLibrisRouter } from "../../router";
import HomeView from "./HomeView.vue";

const mountHomeView = (reader: Reader) =>
  mount(HomeView, {
    global: {
      plugins: [createLibrisI18n(), createLibrisRouter()],
      provide: {
        [meApiKey]: new FakeMeApi(reader),
        [revisionKey]: "sha-abc1234",
      },
    },
  });

describe("HomeView", () => {
  it("renders the French title from the messages", () => {
    const wrapper = mountHomeView(chloe);

    expect(wrapper.text()).toContain("Libris");
  });

  it("greets the reader the API answers by display name", async () => {
    // Given
    const wrapper = mountHomeView(chloe);

    // When
    await flushPromises();

    // Then
    expect(wrapper.text()).toContain("Bonjour Chloé");
  });

  it("has no link to add an ouvrage", () => {
    const screen = within(mountHomeView(chloe).element as HTMLElement);

    expect(
      screen.queryByRole("link", { name: "Ajouter un ouvrage" }),
    ).toBeNull();
  });

  it("shows the revision at the foot of the page", async () => {
    // Given
    const wrapper = mountHomeView(chloe);

    // When
    await flushPromises();

    // Then
    const screen = within(wrapper.element as HTMLElement);
    const revision = screen.getByText("sha-abc1234");
    const greeting = screen.getByText("Bonjour Chloé");
    expect(greeting.compareDocumentPosition(revision)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING,
    );
  });
});
