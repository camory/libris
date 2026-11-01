<script setup lang="ts">
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
      ></div>
      <div class="flex min-w-0 flex-col gap-1.5">
        <p class="text-label text-muted">{{ t("isbn.card.isbn") }}</p>
        <p class="text-body tabular-nums">{{ isbn13 }}</p>
      </div>
    </div>
  </form>
</template>
