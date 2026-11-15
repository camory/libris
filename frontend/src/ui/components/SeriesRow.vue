<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Series } from "../../domain/Series";
import RowCover from "./RowCover.vue";

const props = defineProps<{ series: Series }>();

const { t } = useI18n();

const count = computed(() =>
  t(
    `series.count.${props.series.kind}`,
    { count: props.series.volumeCount },
    props.series.volumeCount,
  ),
);
</script>

<template>
  <RouterLink
    :to="`/catalogue/series/${series.id}`"
    class="flex items-center gap-3.5 p-3.5"
  >
    <RowCover
      :url="series.coverUrl"
      :alt="t('series.cover', { name: series.name })"
    />
    <div class="flex min-w-0 flex-col gap-1.5">
      <p class="text-row-title">{{ series.name }}</p>
      <p class="text-body text-muted">{{ count }}</p>
    </div>
  </RouterLink>
</template>
