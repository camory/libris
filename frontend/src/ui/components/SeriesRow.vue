<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { Series } from "../../domain/Series";
import IconBook from "./icons/IconBook.vue";

defineProps<{ series: Series }>();

const { t } = useI18n();
</script>

<template>
  <div class="flex gap-3.5 p-3.5">
    <div
      class="flex h-18.5 w-12 shrink-0 items-center justify-center rounded-md bg-border"
    >
      <img
        v-if="series.coverUrl"
        :src="series.coverUrl"
        :alt="t('series.cover', { name: series.name })"
        class="h-full w-full rounded-md object-contain"
      />
      <IconBook v-else class="size-5.5 text-muted opacity-60" />
    </div>
    <div class="flex min-w-0 flex-col gap-1.5">
      <p class="text-row-title">{{ series.name }}</p>
    </div>
  </div>
</template>
