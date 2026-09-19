<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import type { AuthorRole, SourceEdition } from "../../domain/SourceEdition";
import IconBook from "./icons/IconBook.vue";

const props = defineProps<{ edition: SourceEdition }>();

const { t, te } = useI18n();

const coverFailed = ref(false);

const overline = computed(() => {
  const series = props.edition.series;
  if (series === null) {
    return null;
  }
  if (series.volumeNumber === null) {
    return series.name;
  }
  return t(`isbn.card.series.${props.edition.kind}`, {
    name: series.name,
    volume: series.volumeNumber,
  });
});

const authorLines = computed(() => {
  const played = new Map<string, AuthorRole[]>();
  for (const author of props.edition.authors) {
    const roles = played.get(author.name) ?? [];
    if (!roles.includes(author.role)) {
      roles.push(author.role);
    }
    played.set(author.name, roles);
  }
  const authors = [...played].map(([name, roles]) => ({ name, roles }));
  const sets = new Set(authors.map(({ roles }) => [...roles].sort().join()));
  if (sets.size === 1) {
    return [{ name: authors.map(({ name }) => name).join(", "), roles: null }];
  }
  return authors.map(({ name, roles }) => ({
    name,
    roles: roles
      .map((role) => t(`role.${props.edition.kind}.${role}`))
      .join(", "),
  }));
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
          v-if="edition.coverUrl && !coverFailed"
          :src="edition.coverUrl"
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
        <template v-for="line in authorLines" :key="line.name">
          <i18n-t
            v-if="line.roles"
            keypath="isbn.card.author"
            tag="p"
            class="text-body"
          >
            <template #name>{{ line.name }}</template>
            <template #roles>
              <span class="text-muted">{{ line.roles }}</span>
            </template>
          </i18n-t>
          <p v-else class="text-body">{{ line.name }}</p>
        </template>
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
  </article>
</template>
