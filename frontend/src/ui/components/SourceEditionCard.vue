<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import type { SourceEdition } from "../../domain/SourceEdition";
import IconBook from "./icons/IconBook.vue";

const props = defineProps<{ edition: SourceEdition }>();

const { t, te } = useI18n();

const coverFailed = ref(false);

const cover = computed(() =>
  coverFailed.value ? null : props.edition.coverUrl,
);

const overline = computed(() => {
  const series = props.edition.series;
  if (series === null) {
    return null;
  }
  if (series.volumeNumber === null) {
    return series.name;
  }
  return t("isbn.card.series", {
    name: series.name,
    volume: series.volumeNumber,
  });
});

const authorLines = computed(() => {
  const words = new Map<string, string[]>();
  for (const author of props.edition.authors) {
    const said = words.get(author.name) ?? [];
    said.push(t(`role.${author.role}`));
    words.set(author.name, said);
  }
  return [...words].map(([name, roles]) => ({ name, roles: roles.join(", ") }));
});

const languageWord = computed(() => {
  const language = props.edition.language;
  if (language === null) {
    return null;
  }
  const word = `language.${language}`;
  return te(word) ? t(word) : language;
});

const rows = computed(() => {
  const edition = props.edition;
  const fields = [
    { label: "collection", value: edition.collection },
    { label: "publisher", value: edition.publisher },
    { label: "year", value: edition.publicationYear },
    { label: "language", value: languageWord.value },
    { label: "pages", value: edition.pageCount },
    { label: "isbn", value: edition.isbn13 },
  ];
  return fields.filter((field) => field.value !== null);
});
</script>

<template>
  <article
    class="flex flex-col gap-3.5 rounded-[14px] border border-border bg-surface p-4"
  >
    <div class="flex gap-3.5">
      <div
        class="flex h-[149px] w-24 shrink-0 items-center justify-center rounded-md bg-border"
      >
        <img
          v-if="cover"
          :src="cover"
          :alt="t('isbn.card.cover', { title: edition.title })"
          class="h-full w-full rounded-md object-contain"
          @error="coverFailed = true"
        />
        <IconBook v-else class="text-muted opacity-60" />
      </div>

      <div class="flex min-w-0 flex-col gap-1.5">
        <h2 class="flex flex-col gap-1.5 text-card-title">
          <span v-if="overline" class="text-overline uppercase text-accent">
            {{ overline }}
          </span>
          {{ edition.title }}
        </h2>
        <p v-if="edition.subtitle" class="text-lead text-muted">
          {{ edition.subtitle }}
        </p>
        <i18n-t
          v-for="line in authorLines"
          :key="line.name"
          keypath="isbn.card.author"
          tag="p"
          class="text-body"
        >
          <template #name>{{ line.name }}</template>
          <template #roles>
            <span class="text-muted">{{ line.roles }}</span>
          </template>
        </i18n-t>
      </div>
    </div>

    <div>
      <div
        v-for="row in rows"
        :key="row.label"
        class="flex justify-between gap-3.5 border-t border-border py-[7px] text-body"
      >
        <span class="text-muted">{{ t(`isbn.card.${row.label}`) }}</span>
        <span class="text-right tabular-nums">{{ row.value }}</span>
      </div>
    </div>

    <p v-if="edition.summary" class="text-body">{{ edition.summary }}</p>

    <div class="flex flex-wrap items-center gap-2">
      <span class="text-label text-muted">{{ t("isbn.card.sources") }}</span>
      <span
        v-for="source in edition.sources"
        :key="source"
        class="rounded-full border border-border px-2.5 py-1 text-label"
      >
        {{ t(`source.${source}`) }}
      </span>
    </div>
  </article>
</template>
