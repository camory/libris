<script setup lang="ts">
import { CircleAlert } from "@lucide/vue";
import { computed, inject, watch } from "vue";
import { useI18n } from "vue-i18n";
import { seriesApiKey } from "../../../application/SeriesApi";
import { useFindCatalogueSeries } from "../../../application/useFindCatalogueSeries";
import { booksInOrder } from "../../../domain/Book";
import CatalogueRow from "../../components/CatalogueRow.vue";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
import FieldRowSkeleton from "../../components/FieldRowSkeleton.vue";
import PageHintSkeleton from "../../components/PageHintSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";
import SeriesCard from "../../components/SeriesCard.vue";

const props = defineProps<{ id: string }>();

const { t, locale } = useI18n();
const { state, find } = useFindCatalogueSeries(inject(seriesApiKey)!);

const hint = computed(() => {
  if (state.value.status !== "found") {
    return "";
  }
  const { kind, volumeCount } = state.value.series;
  return t("seriesPage.hint", {
    count: t(`series.count.${kind}`, { count: volumeCount }, volumeCount),
  });
});

const booksOnTheShelf = computed(() =>
  state.value.status === "found"
    ? booksInOrder(state.value.series.books, locale.value)
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
      <header class="pt-5 pb-3">
        <h1 class="text-page-title">{{ state.series.name }}</h1>
        <p class="text-body text-muted">{{ hint }}</p>
      </header>
      <SeriesCard :series="state.series" />
      <ul
        class="mt-5 divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
      >
        <li v-for="book in booksOnTheShelf" :key="book.id">
          <CatalogueRow :book="book" without-series-name />
        </li>
      </ul>
    </template>
    <p v-else class="flex items-start gap-2 pt-5 text-body text-danger">
      <CircleAlert :size="22" class="shrink-0" />
      <span>{{ t("seriesPage.notFound") }}</span>
    </p>
  </main>
</template>
