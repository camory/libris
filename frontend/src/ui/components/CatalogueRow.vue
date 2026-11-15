<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Book } from "../../domain/Book";
import { bookshelvesHolding } from "../../domain/Copy";
import { authorsInOrder } from "../../domain/SourceEdition";
import RowCover from "./RowCover.vue";

const props = defineProps<{ book: Book; withoutSeriesName?: boolean }>();

const { t, locale } = useI18n();

const overline = computed(() => {
  const series = props.book.series;
  if (series === null) {
    return null;
  }
  if (series.volumeNumber === null) {
    return props.withoutSeriesName ? null : series.name;
  }
  if (props.withoutSeriesName) {
    return t(`catalogue.volume.${props.book.kind}`, {
      volume: series.volumeNumber,
    });
  }
  return t(`catalogue.series.${props.book.kind}`, {
    name: series.name,
    volume: series.volumeNumber,
  });
});

const authors = computed(() =>
  authorsInOrder(props.book.authors, locale.value)
    .map(({ name }) => name)
    .join(", "),
);

const bookshelves = computed(() =>
  bookshelvesHolding(props.book.copies, locale.value)
    .map(({ bookshelf, count }) =>
      t("catalogue.copies", { bookshelf: bookshelf.name, count }, count),
    )
    .join(", "),
);
</script>

<template>
  <RouterLink :to="`/catalogue/ouvrages/${book.id}`" class="flex gap-3.5 p-3.5">
    <RowCover
      :url="book.coverUrl"
      :alt="t('catalogue.cover', { title: book.title })"
    />
    <div class="flex min-w-0 flex-col gap-1.5">
      <p v-if="overline" class="text-overline uppercase text-accent">
        {{ overline }}
      </p>
      {{ " " }}
      <p class="text-row-title">{{ book.title }}</p>
      {{ " " }}
      <p class="text-body">{{ authors }}</p>
      {{ " " }}
      <p class="text-body text-muted">{{ bookshelves }}</p>
    </div>
  </RouterLink>
</template>
