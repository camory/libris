import { within } from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { meApiKey } from "../../../application/MeApi";
import type { Reader } from "../../../domain/Reader";
import { FakeMeApi } from "../../../fixture/FakeMeApi";
import { createLibrisI18n } from "../../i18n";
import { revisionKey } from "../../revision";
import { createLibrisRouter } from "../../router";
import HomeView from "./HomeView.vue";

const chloe: Reader = {
  id: "0199c0de-1000-7000-8000-000000000001",
  username: "chloe",
  displayName: "Chloé",
  email: "chloe@amory.fr",
  role: "READER",
};

const mountHomeView = (reader: Reader) =>
  mount(HomeView, {
    global: {
      plugins: [createLibrisI18n(), createLibrisRouter()],
      provide: { [meApiKey]: new FakeMeApi(reader), [revisionKey]: "sha-abc1234" },
    },
  });

describe("HomeView", () => {
  it("renders the French title from the messages", () => {
    const wrapper = mountHomeView(chloe);

    expect(wrapper.text()).toContain("La bibliothèque de la maison");
  });

  it("greets the reader the API answers by display name", async () => {
    // Given
    const wrapper = mountHomeView(chloe);

    // When
    await flushPromises();

    // Then
    expect(wrapper.text()).toContain("Bonjour Chloé");
  });

  it("links to the screen that adds an ouvrage", () => {
    const screen = within(mountHomeView(chloe).element as HTMLElement);

    expect(
      screen
        .getByRole("link", { name: "Ajouter un ouvrage" })
        .getAttribute("href"),
    ).toBe("/isbn");
  });

  it("shows the revision at the foot of the page", () => {
    // When
    const screen = within(mountHomeView(chloe).element as HTMLElement);

    // Then
    const revision = screen.getByText("sha-abc1234");
    const link = screen.getByRole("link", { name: "Ajouter un ouvrage" });
    expect(link.compareDocumentPosition(revision)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING,
    );
  });
});
