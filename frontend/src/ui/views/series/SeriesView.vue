<script setup lang="ts">
import { computed, inject } from "vue";
import { useI18n } from "vue-i18n";
import { seriesApiKey } from "../../../application/SeriesApi";
import { useListCatalogueSeries } from "../../../application/useListCatalogueSeries";
import { seriesInOrder } from "../../../domain/Series";
import CatalogueSwitch from "../../components/CatalogueSwitch.vue";
import SeriesRow from "../../components/SeriesRow.vue";
import SeriesRowSkeleton from "../../components/SeriesRowSkeleton.vue";
import IconAlert from "../../components/icons/IconAlert.vue";
import IconBook from "../../components/icons/IconBook.vue";

const { t, locale } = useI18n();
const { state, list } = useListCatalogueSeries(inject(seriesApiKey)!);

const seriesByName = computed(() =>
  state.value.status === "listed"
    ? seriesInOrder(state.value.series, locale.value)
    : [],
);

void list();
</script>

<template>
  <main class="mx-auto flex min-h-full w-full max-w-120 flex-col px-5 pb-5">
    <header class="pt-5 pb-3">
      <h1 class="text-page-title">{{ t("catalogue.title") }}</h1>
      <p class="text-body text-muted">{{ t("catalogue.hint") }}</p>
    </header>
    <CatalogueSwitch class="mb-5" />

    <div
      v-if="state.status === 'loading'"
      class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
    >
      <SeriesRowSkeleton v-for="row in 5" :key="row" />
    </div>
    <ul
      v-else-if="state.status === 'listed'"
      class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
    >
      <li v-for="series in seriesByName" :key="series.name">
        <SeriesRow :series="series" />
      </li>
    </ul>
    <p
      v-else-if="state.status === 'unavailable'"
      class="flex items-start gap-2 text-body text-danger"
    >
      <IconAlert />
      <span>{{ t("series.error") }}</span>
    </p>
    <div v-else class="flex flex-col items-center gap-3 p-7 text-center">
      <IconBook class="text-muted opacity-60" />
      <p class="text-lead text-muted">{{ t("series.empty") }}</p>
    </div>
  </main>
</template>
