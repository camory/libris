<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Book } from "../../domain/Book";

const props = defineProps<{ book: Book }>();

const { t } = useI18n();

const overline = computed(() => {
  const series = props.book.series;
  if (series === null) {
    return null;
  }
  if (series.volumeNumber === null) {
    return series.name;
  }
  return t("catalogue.series", {
    name: series.name,
    volume: series.volumeNumber,
  });
});

const authors = computed(() =>
  [...new Set(props.book.authors.map((author) => author.name))].join(", "),
);

const bookshelves = computed(() => {
  const names = new Map<string, string>();
  for (const copy of props.book.copies) {
    names.set(copy.bookshelf.id, copy.bookshelf.name);
  }
  return [...names.values()].join(", ");
});
</script>

<template>
  <div class="flex gap-3.5 p-3.5">
    <div class="flex min-w-0 flex-col gap-1.5">
      <p v-if="overline" class="text-overline uppercase text-accent">
        {{ overline }}
      </p>
      <p class="text-row-title">{{ book.title }}</p>
      <p class="text-body">{{ authors }}</p>
      <p class="text-body text-muted">{{ bookshelves }}</p>
    </div>
  </div>
</template>
