<script setup lang="ts">
import { BookOpen } from "@lucide/vue";
import { computed, onMounted, ref, useTemplateRef, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { Book } from "../../domain/Book";
import { bookshelvesHolding, type Copy } from "../../domain/Copy";
import type { CoverCandidate } from "../../domain/Cover";
import { languageWord } from "../../domain/Language";
import type { SourceEdition } from "../../domain/SourceEdition";
import AuthorLines from "./AuthorLines.vue";

const props = defineProps<{
  edition: SourceEdition | Book;
  copies: Copy[];
  covers: CoverCandidate[];
  held: boolean;
  pageTitle?: boolean;
}>();

const emit = defineEmits<{ coverSource: [source: string | null] }>();

const { t, locale } = useI18n();

const failed = ref<number[]>([]);
const offered = computed(() =>
  props.covers
    .map((cover, index) => ({ cover, index }))
    .filter(({ index }) => !failed.value.includes(index)),
);
const tapped = ref<number | null>(null);
const shown = computed(
  () =>
    offered.value.find(({ index }) => index === tapped.value) ??
    offered.value[0],
);

watch(
  () => shown.value?.cover.source ?? null,
  (source) => emit("coverSource", source),
  { immediate: true },
);

function isShown(index: number) {
  return index === shown.value?.index;
}

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

const bookshelves = computed(() =>
  bookshelvesHolding(props.copies, locale.value),
);

const language = computed(() => {
  const code = props.edition.language;
  return code === null ? null : languageWord(code, locale.value);
});

const rows = computed(() => {
  const edition = props.edition;
  const fields = [
    { label: "collection", value: edition.collection },
    { label: "publisher", value: edition.publisher },
    { label: "year", value: edition.publicationYear },
    { label: "language", value: language.value },
    { label: "pages", value: edition.pageCount },
    { label: "isbn", value: edition.isbn13 },
  ];
  return fields.filter((field) => field.value !== null);
});

const summaryBlock = useTemplateRef("summaryBlock");
const overflows = ref(false);
const unfolded = ref(false);
onMounted(() => {
  const block = summaryBlock.value;
  overflows.value = block !== null && block.scrollHeight > block.clientHeight;
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
            :aria-hidden="!isShown(index)"
            class="h-full w-full rounded-md object-contain"
            :class="{ hidden: !isShown(index) }"
            @error="failed.push(index)"
          />
          <BookOpen
            v-if="!shown"
            :size="48"
            class="shrink-0 text-muted opacity-60"
          />
        </div>
        <div v-if="!held" class="flex justify-center">
          <button
            v-for="{ cover, index } in offered"
            :key="cover.url"
            type="button"
            :aria-label="t('isbn.card.coverSource', { source: cover.source })"
            :aria-pressed="isShown(index)"
            class="relative -my-3 flex h-11 w-8 items-center justify-center"
            @click="tapped = index"
          >
            <span
              class="size-2 rounded-full"
              :class="
                isShown(index) ? 'bg-accent' : 'border-[1.5px] border-muted'
              "
            ></span>
          </button>
        </div>
        <p v-if="!held && shown" class="text-center text-body text-muted">
          {{ shown.cover.source }}
        </p>
      </div>

      <div class="flex min-w-0 flex-col gap-1.5">
        <p v-if="overline" class="text-overline uppercase text-accent">
          {{ overline }}
        </p>
        {{ " " }}
        <component :is="pageTitle ? 'h1' : 'h2'" class="text-card-title">
          {{ edition.title }}
        </component>
        <p v-if="edition.subtitle" class="text-lead text-muted">
          {{ edition.subtitle }}
        </p>
        <AuthorLines :authors="edition.authors" :kind="edition.kind" />
      </div>
    </div>

    <div class="text-body font-semibold">
      <div
        v-for="{ bookshelf, count } in bookshelves"
        :key="bookshelf.id"
        class="flex items-center gap-2"
      >
        <BookOpen :size="22" class="shrink-0" />
        <i18n-t keypath="isbn.card.copies" :plural="count" tag="p">
          <template #bookshelf>{{ bookshelf.name }}</template>
          <template #count>{{ count }}</template>
        </i18n-t>
      </div>
      <div v-if="bookshelves.length === 0" class="flex items-center gap-2">
        <BookOpen :size="22" class="shrink-0" />
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

    <div v-if="edition.summary">
      <div
        ref="summaryBlock"
        class="space-y-2 text-body"
        :class="{ 'line-clamp-6': !unfolded }"
      >
        <p v-for="(line, index) in edition.summary.split('\n')" :key="index">
          {{ line }}
        </p>
      </div>
      <button
        v-if="overflows && !unfolded"
        type="button"
        class="-ml-3 h-11 px-3 text-button text-accent"
        @click="unfolded = true"
      >
        {{ t("isbn.card.readMore") }}
      </button>
    </div>
  </article>
</template>
