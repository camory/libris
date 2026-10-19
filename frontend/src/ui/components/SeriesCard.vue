<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { runsOf, type Run } from "../../domain/Run";
import type { SeriesWithBooks } from "../../domain/Series";

const props = defineProps<{ series: SeriesWithBooks }>();

const { t } = useI18n();

const rows = computed(() => {
  const { owned, missing } = runsOf(props.series.books);
  return [
    { label: t("seriesPage.owned"), value: read(owned) },
    { label: t("seriesPage.missing"), value: read(missing) },
  ].filter(({ value }) => value !== "");
});

function read(runs: Run[]): string {
  return runs
    .map(({ first, last }) =>
      t("seriesPage.run", { first, last }, last - first),
    )
    .join(", ");
}
</script>

<template>
  <div
    class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
  >
    <div
      v-for="row in rows"
      :key="row.label"
      class="flex justify-between gap-3.5 p-3.5 text-body"
    >
      <span class="text-muted">{{ row.label }}</span>
      <span class="text-right tabular-nums">{{ row.value }}</span>
    </div>
  </div>
</template>
