import { CircleAlert } from "@lucide/vue";
import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import {
  catalogueApiKey,
  type BookPage,
  type CatalogueApi,
  type FindBookAnswer,
} from "../../../application/CatalogueApi";
import { asterixEtSesAmis, asterixLeGaulois } from "../../../fixture/Books";
import { FakeCatalogueApi } from "../../../fixture/FakeCatalogueApi";
import { normalised } from "../../../fixture/normalised";
import HeldEditionCardSkeleton from "../../components/HeldEditionCardSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";
import { createLibrisI18n } from "../../i18n";
import EditionPageView from "./EditionPageView.vue";

const mountEditionPageView = (
  found: FindBookAnswer | CatalogueApi,
  id = asterixLeGaulois.id,
) =>
  mount(EditionPageView, {
    props: { id },
    global: {
      plugins: [createLibrisI18n()],
      provide: {
        [catalogueApiKey]:
          "outcome" in found ? new FakeCatalogueApi([], found) : found,
      },
    },
  });

class LibrisRecordingAsks implements CatalogueApi {
  readonly asked: string[] = [];

  browse(): Promise<BookPage> {
    return Promise.resolve({ books: [], next: null });
  }

  find(id: string): Promise<FindBookAnswer> {
    this.asked.push(id);
    return Promise.resolve(noSuchOuvrage);
  }
}

describe("EditionPageView", () => {
  it("shows the card's skeleton while the ouvrage comes, no title", async () => {
    // Given
    const wrapper = mountEditionPageView(noSuchOuvrage);
    const before = seen(wrapper);

    // When
    await flushPromises();

    // Then
    expect([before, seen(wrapper)]).toEqual([
      { card: 1, title: 0 },
      { card: 0, title: 0 },
    ]);
  });

  it("says there is no such ouvrage, alone", async () => {
    // Given
    const wrapper = mountEditionPageView(noSuchOuvrage);

    // When
    await flushPromises();

    // Then
    expect(normalised(wrapper.element)).toBe("Ouvrage introuvable.");
    expect(wrapper.findAllComponents(CircleAlert)).toHaveLength(1);
  });

  it("asks the ouvrage of each id it is given", async () => {
    // Given
    const libris = new LibrisRecordingAsks();
    const wrapper = mountEditionPageView(libris, asterixLeGaulois.id);
    await flushPromises();

    // When
    await wrapper.setProps({ id: asterixEtSesAmis.id });
    await flushPromises();

    // Then
    expect(libris.asked).toEqual([asterixLeGaulois.id, asterixEtSesAmis.id]);
  });
});

function seen(wrapper: ReturnType<typeof mountEditionPageView>) {
  return {
    card: wrapper.findAllComponents(HeldEditionCardSkeleton).length,
    title: wrapper.findAllComponents(PageTitleSkeleton).length,
  };
}

const noSuchOuvrage: FindBookAnswer = {
  outcome: "problem",
  type: "/problems/not-found",
};
