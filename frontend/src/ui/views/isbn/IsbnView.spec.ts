import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { isbnApiKey, type IsbnAnswer } from "../../../application/IsbnApi";
import { FakeIsbnApi } from "../../../fixture/FakeIsbnApi";
import { createLibrisI18n } from "../../i18n";
import IsbnView from "./IsbnView.vue";

type Screen = BoundFunctions<typeof queries>;

const unknownIsbn: IsbnAnswer = {
  outcome: "problem",
  type: "/problems/not-found",
};

describe("IsbnView", () => {
  it("renders the title and the hint", () => {
    const screen = open(new FakeIsbnApi(unknownIsbn));

    expect(screen.getByText("Ajouter un ouvrage")).toBeDefined();
    expect(
      screen.getByText("Scannez le code-barres ou saisissez l'ISBN."),
    ).toBeDefined();
  });

  it("refuses a text the rule refuses and asks nothing", async () => {
    // Given
    const api = new FakeIsbnApi(unknownIsbn);
    const screen = open(api);

    // When
    await ask(screen, "978272348852");

    // Then
    expect(screen.getByText("ISBN invalide")).toBeDefined();
    expect(field(screen).value).toBe("978272348852");
    expect(api.asked).toEqual([]);
  });

  function open(api: FakeIsbnApi) {
    const wrapper = mount(IsbnView, {
      global: {
        plugins: [createLibrisI18n()],
        provide: { [isbnApiKey]: api },
      },
    });
    return within(wrapper.element as HTMLElement);
  }

  async function ask(screen: Screen, text: string) {
    await fireEvent.input(field(screen), { target: { value: text } });
    await fireEvent.click(screen.getByRole("button", { name: "Chercher" }));
    await flushPromises();
  }

  function field(screen: Screen) {
    return screen.getByRole<HTMLInputElement>("textbox", { name: "ISBN" });
  }
});
