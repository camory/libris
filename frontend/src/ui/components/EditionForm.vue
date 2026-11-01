<script setup lang="ts">
import { BookOpen } from "@lucide/vue";
import { useI18n } from "vue-i18n";

defineProps<{ isbn13: string }>();

const { t } = useI18n();
</script>

<template>
  <form
    class="flex flex-col gap-3.5 rounded-[14px] border border-border bg-surface p-4"
  >
    <div class="flex gap-3.5">
      <div
        class="flex h-[149px] w-24 shrink-0 items-center justify-center rounded-md bg-border"
      >
        <BookOpen :size="48" class="shrink-0 text-muted opacity-60" />
      </div>
      <div class="flex min-w-0 flex-col gap-1.5">
        <p class="text-label text-muted">{{ t("isbn.card.isbn") }}</p>
        <p class="text-body tabular-nums">{{ isbn13 }}</p>
      </div>
    </div>

    <div class="flex items-center gap-2 text-body font-semibold">
      <BookOpen :size="22" class="shrink-0" />
      <p>{{ t("isbn.card.noCopy") }}</p>
    </div>
  </form>
</template>
