import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import {
  catalogueApiKey,
  type FindBookAnswer,
} from "../../../application/CatalogueApi";
import { asterixLeGaulois } from "../../../fixture/Books";
import { FakeCatalogueApi } from "../../../fixture/FakeCatalogueApi";
import HeldEditionCardSkeleton from "../../components/HeldEditionCardSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";
import { createLibrisI18n } from "../../i18n";
import EditionPageView from "./EditionPageView.vue";

const mountEditionPageView = (
  found: FindBookAnswer,
  id = asterixLeGaulois.id,
) =>
  mount(EditionPageView, {
    props: { id },
    global: {
      plugins: [createLibrisI18n()],
      provide: { [catalogueApiKey]: new FakeCatalogueApi([], found) },
    },
  });

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
