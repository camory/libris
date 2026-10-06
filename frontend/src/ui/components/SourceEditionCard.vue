<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import type { Copy } from "../../domain/Copy";
import type { CoverCandidate } from "../../domain/Cover";
import type { AuthorRole, SourceEdition } from "../../domain/SourceEdition";
import IconBook from "./icons/IconBook.vue";

const props = defineProps<{
  edition: SourceEdition;
  copies: Copy[];
  covers: CoverCandidate[];
}>();

const { t, te } = useI18n();

const failed = ref<number[]>([]);
const offered = computed(() =>
  props.covers
    .map((cover, index) => ({ cover, index }))
    .filter(({ index }) => !failed.value.includes(index)),
);
const shown = computed(() => offered.value[0]?.index ?? -1);
const shownCover = computed(() => props.covers[shown.value]);

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

const bookshelves = computed(() => {
  const held = new Map<string, { name: string; count: number }>();
  for (const copy of props.copies) {
    const bookshelf = held.get(copy.bookshelf.id) ?? {
      name: copy.bookshelf.name,
      count: 0,
    };
    bookshelf.count += 1;
    held.set(copy.bookshelf.id, bookshelf);
  }
  return [...held].map(([id, { name, count }]) => ({ id, name, count }));
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
      <div class="flex w-24 shrink-0 flex-col">
        <div
          class="flex h-[149px] w-24 shrink-0 items-center justify-center rounded-md bg-border"
        >
          <img
            v-for="(cover, index) in covers"
            :key="cover.url"
            :src="cover.url"
            :alt="t('isbn.card.cover', { title: edition.title })"
            :aria-hidden="index !== shown"
            class="h-full w-full rounded-md object-contain"
            :class="{ hidden: index !== shown }"
            @error="failed.push(index)"
          />
          <IconBook v-if="shown === -1" class="text-muted opacity-60" />
        </div>
        <div>
          <button
            v-for="{ cover, index } in offered"
            :key="cover.url"
            type="button"
            :aria-pressed="index === shown"
            :aria-label="t('isbn.card.coverSource', { source: cover.source })"
          ></button>
        </div>
        <p v-if="shownCover">{{ shownCover.source }}</p>
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

    <div class="text-body font-semibold">
      <div
        v-for="bookshelf in bookshelves"
        :key="bookshelf.id"
        class="flex items-center gap-2"
      >
        <IconBook class="size-5.5" />
        <i18n-t keypath="isbn.card.copies" :plural="bookshelf.count" tag="p">
          <template #bookshelf>{{ bookshelf.name }}</template>
          <template #count>{{ bookshelf.count }}</template>
        </i18n-t>
      </div>
      <div v-if="bookshelves.length === 0" class="flex items-center gap-2">
        <IconBook class="size-5.5" />
        <p>{{ t("isbn.card.noCopy") }}</p>
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
