<script setup lang="ts">
import { computed, onMounted, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import type { Book } from "../../domain/Book";
import type { Copy } from "../../domain/Copy";
import type { CoverCandidate } from "../../domain/Cover";
import { languageWord } from "../../domain/Language";
import type { SourceEdition } from "../../domain/SourceEdition";
import AuthorLines from "./AuthorLines.vue";
import BookshelvesHolding from "./BookshelvesHolding.vue";
import CoverChoice from "./CoverChoice.vue";

const props = defineProps<{
  edition: SourceEdition | Book;
  copies: Copy[];
  covers: CoverCandidate[];
  held: boolean;
  pageTitle?: boolean;
}>();

const emit = defineEmits<{ coverSource: [source: string | null] }>();

const { t, locale } = useI18n();

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
      <CoverChoice
        :covers="covers"
        :title="edition.title"
        :held="held"
        @cover-source="emit('coverSource', $event)"
      />

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

    <BookshelvesHolding :copies="copies" />

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
