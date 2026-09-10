import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { meApiKey } from "../../../application/MeApi";
import type { Reader } from "../../../domain/Reader";
import { FakeMeApi } from "../../../fixture/FakeMeApi";
import { i18n } from "../../i18n";
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
      plugins: [i18n],
      provide: { [meApiKey]: new FakeMeApi(reader) },
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
});
