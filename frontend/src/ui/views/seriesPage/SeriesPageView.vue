<script setup lang="ts">
import { CircleAlert } from "@lucide/vue";
import { computed, inject, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { seriesApiKey } from "../../../application/SeriesApi";
import { useFindCatalogueSeries } from "../../../application/useFindCatalogueSeries";
import { booksAndGapsInOrder } from "../../../domain/Book";
import CatalogueRow from "../../components/CatalogueRow.vue";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
import FieldRowSkeleton from "../../components/FieldRowSkeleton.vue";
import GapRow from "../../components/GapRow.vue";
import PageHintSkeleton from "../../components/PageHintSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";
import SeriesCard from "../../components/SeriesCard.vue";

const props = defineProps<{ id: string }>();

const { t, locale } = useI18n();
const { state, find } = useFindCatalogueSeries(inject(seriesApiKey)!);

const failedCover = ref<string | null>(null);

const band = computed(() =>
  state.value.status === "found" &&
  state.value.series.coverUrl !== failedCover.value
    ? state.value.series.coverUrl
    : null,
);

const hint = computed(() => {
  if (state.value.status !== "found") {
    return "";
  }
  const { kind, volumeCount } = state.value.series;
  return t("seriesPage.hint", {
    count: t(`series.count.${kind}`, { count: volumeCount }, volumeCount),
  });
});

const booksAndGaps = computed(() =>
  state.value.status === "found"
    ? booksAndGapsInOrder(state.value.series.books, locale.value)
    : [],
);

watch(
  () => props.id,
  (id) => void find(id),
  { immediate: true },
);
</script>

<template>
  <main class="mx-auto w-full max-w-120 px-5 pb-5">
    <template v-if="state.status === 'loading'">
      <header class="pt-5 pb-3">
        <PageTitleSkeleton />
        <PageHintSkeleton />
      </header>
      <div
        class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
      >
        <FieldRowSkeleton v-for="row in 2" :key="row" />
      </div>
      <div
        class="mt-5 divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
      >
        <CatalogueRowSkeleton v-for="row in 5" :key="row" />
      </div>
    </template>
    <template v-else-if="state.status === 'found'">
      <img
        v-if="band"
        :src="band"
        :alt="t('series.cover', { name: state.series.name })"
        class="-mx-5 h-32.5 w-[calc(100%+2.5rem)] max-w-none object-cover object-top"
        @error="failedCover = band"
      />
      <header class="pb-3" :class="band ? 'pt-3' : 'pt-5'">
        <h1 class="text-page-title">{{ state.series.name }}</h1>
        <p class="text-body text-muted">{{ hint }}</p>
      </header>
      <div class="flex flex-col gap-5">
        <SeriesCard :series="state.series" />
        <ul
          class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
        >
          <li
            v-for="each in booksAndGaps"
            :key="'book' in each ? each.book.id : `gap ${each.gap.first}`"
          >
            <CatalogueRow
              v-if="'book' in each"
              :book="each.book"
              without-series-name
            />
            <GapRow v-else :gap="each.gap" :kind="state.series.kind" />
          </li>
        </ul>
      </div>
    </template>
    <p v-else class="flex items-start gap-2 pt-5 text-body text-danger">
      <CircleAlert :size="22" class="shrink-0" />
      <span>{{
        t(
          state.status === "unavailable"
            ? "seriesPage.error"
            : "seriesPage.notFound",
        )
      }}</span>
    </p>
  </main>
</template>
