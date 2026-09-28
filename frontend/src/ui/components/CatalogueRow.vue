<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Book } from "../../domain/Book";

const props = defineProps<{ book: Book }>();

const { t } = useI18n();

const overline = computed(() =>
  t("catalogue.series", {
    name: props.book.series?.name,
    volume: props.book.series?.volumeNumber,
  }),
);
</script>

<template>
  <div class="flex gap-3.5 p-3.5">
    <div class="flex min-w-0 flex-col gap-1.5">
      <p class="text-overline uppercase text-accent">{{ overline }}</p>
      <p class="text-row-title">{{ book.title }}</p>
    </div>
  </div>
</template>
