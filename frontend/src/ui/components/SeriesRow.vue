<script setup lang="ts">
import { BookOpen } from "@lucide/vue";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import type { Series } from "../../domain/Series";

const props = defineProps<{ series: Series }>();

const { t } = useI18n();

const coverFailed = ref(false);

const count = computed(() =>
  t(
    `series.count.${props.series.kind}`,
    { count: props.series.volumeCount },
    props.series.volumeCount,
  ),
);
</script>

<template>
  <div class="flex items-center gap-3.5 p-3.5">
    <div
      class="flex h-18.5 w-12 shrink-0 items-center justify-center rounded-md bg-border"
    >
      <img
        v-if="series.coverUrl && !coverFailed"
        :src="series.coverUrl"
        :alt="t('series.cover', { name: series.name })"
        class="h-full w-full rounded-md object-contain"
        @error="coverFailed = true"
      />
      <BookOpen v-else :size="22" class="shrink-0 text-muted opacity-60" />
    </div>
    <div class="flex min-w-0 flex-col gap-1.5">
      <p class="text-row-title">{{ series.name }}</p>
      <p class="text-body text-muted">{{ count }}</p>
    </div>
  </div>
</template>
