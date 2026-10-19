<script setup lang="ts">
import { BookOpen } from "@lucide/vue";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import type { Book } from "../../domain/Book";
import { bookshelvesHolding } from "../../domain/Copy";
import { authorsInOrder } from "../../domain/SourceEdition";

const props = defineProps<{ book: Book; withoutSeriesName?: boolean }>();

const { t, locale } = useI18n();

const coverFailed = ref(false);

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
  <div class="flex gap-3.5 p-3.5">
    <div
      class="flex h-18.5 w-12 shrink-0 items-center justify-center rounded-md bg-border"
    >
      <img
        v-if="book.coverUrl && !coverFailed"
        :src="book.coverUrl"
        :alt="t('catalogue.cover', { title: book.title })"
        class="h-full w-full rounded-md object-contain"
        @error="coverFailed = true"
      />
      <BookOpen v-else :size="22" class="shrink-0 text-muted opacity-60" />
    </div>
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
  </div>
</template>
