<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { SourceEdition } from "../../domain/SourceEdition";

const props = defineProps<{ edition: SourceEdition }>();

const { t } = useI18n();

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
  const roles = new Map<string, string[]>();
  for (const author of props.edition.authors) {
    const said = roles.get(author.name) ?? [];
    said.push(t(`role.${author.role}`));
    roles.set(author.name, said);
  }
  return [...roles].map(([name, said]) => ({ name, roles: said.join(", ") }));
});

const languageWord = computed(() => {
  const language = props.edition.language;
  return language === null ? null : t(`language.${language}`);
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
      <img
        v-if="edition.coverUrl"
        :src="edition.coverUrl"
        :alt="t('isbn.card.cover', { title: edition.title })"
        class="h-[149px] w-24 shrink-0 rounded-md bg-border object-contain"
      />
      <div v-else class="h-[149px] w-24 shrink-0 rounded-md bg-border"></div>

      <div class="flex min-w-0 flex-col gap-1.5">
        <p v-if="overline" class="text-overline uppercase text-accent">
          {{ overline }}
        </p>
        <h2 class="text-card-title">{{ edition.title }}</h2>
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
