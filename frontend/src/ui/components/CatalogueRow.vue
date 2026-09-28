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
  const held = new Map<string, { name: string; count: number }>();
  for (const copy of props.book.copies) {
    const bookshelf = held.get(copy.bookshelf.id) ?? {
      name: copy.bookshelf.name,
      count: 0,
    };
    bookshelf.count += 1;
    held.set(copy.bookshelf.id, bookshelf);
  }
  return [...held.values()]
    .map(({ name, count }) =>
      t("catalogue.copies", { bookshelf: name, count }, count),
    )
    .join(", ");
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
