<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import type { Series } from "../../domain/Series";
import IconBook from "./icons/IconBook.vue";

defineProps<{ series: Series }>();

const { t } = useI18n();

const coverFailed = ref(false);
</script>

<template>
  <div class="flex gap-3.5 p-3.5">
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
      <IconBook v-else class="size-5.5 text-muted opacity-60" />
    </div>
    <div class="flex min-w-0 flex-col gap-1.5">
      <p class="text-row-title">{{ series.name }}</p>
      <p class="text-body text-muted">
        {{
          t(
            "series.count.BD",
            { count: series.volumeCount },
            series.volumeCount,
          )
        }}
      </p>
    </div>
  </div>
</template>
