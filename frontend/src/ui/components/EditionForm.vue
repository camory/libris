<script setup lang="ts">
import { reactive, ref, useId } from "vue";
import { useI18n } from "vue-i18n";
import type { Copy } from "../../domain/Copy";
import type { CoverCandidate } from "../../domain/Cover";
import { editionOf } from "../../domain/EditionDraft";
import { languageWord } from "../../domain/Language";
import type { Kind, SourceEdition } from "../../domain/SourceEdition";
import AuthorLines from "./AuthorLines.vue";
import BookshelvesHolding from "./BookshelvesHolding.vue";
import CoverChoice from "./CoverChoice.vue";
import DockedAdd from "./DockedAdd.vue";

const props = defineProps<{
  isbn13: string;
  edition?: SourceEdition;
  copies: Copy[];
  covers: CoverCandidate[];
  adding: boolean;
  failed: boolean;
}>();

const emit = defineEmits<{
  coverSource: [source: string | null];
  add: [edition: SourceEdition];
}>();

const { t, locale } = useI18n();

const kinds: Kind[] = ["BOOK", "BD", "MANGA"];
const answered = props.edition;
const kind = ref<Kind>(answered?.kind ?? "BOOK");

const fields = [
  "title",
  "subtitle",
  "series",
  "volume",
  "collection",
  "publisher",
  "year",
  "language",
  "pages",
] as const;
const numeric = new Set<string>(["volume", "year", "pages"]);
const language = answered?.language ?? null;
const values = reactive<Record<(typeof fields)[number], string>>({
  title: text(answered?.title),
  subtitle: text(answered?.subtitle),
  series: text(answered?.series?.name),
  volume: text(answered?.series?.volumeNumber),
  collection: text(answered?.collection),
  publisher: text(answered?.publisher),
  year: text(answered?.publicationYear),
  language: language === null ? "" : languageWord(language, locale.value),
  pages: text(answered?.pageCount),
});
const summary = ref(text(answered?.summary));

function text(value: string | number | null | undefined) {
  return value == null ? "" : String(value);
}

const id = useId();

function add() {
  emit(
    "add",
    editionOf(
      props.isbn13,
      answered?.authors ?? [],
      { kind: kind.value, ...values, summary: summary.value },
      locale.value,
    ),
  );
}
</script>

<template>
  <div class="flex flex-col">
    <form
      class="flex flex-col gap-3.5 rounded-[14px] border border-border bg-surface p-4"
    >
      <div class="flex gap-3.5">
        <CoverChoice
          :covers="covers"
          :title="edition?.title ?? ''"
          :held="false"
          @cover-source="emit('coverSource', $event)"
        />
        <div class="flex min-w-0 flex-col gap-1.5">
          <p class="text-label text-muted">{{ t("isbn.card.isbn") }}</p>
          <p class="text-body tabular-nums">{{ isbn13 }}</p>
        </div>
      </div>

      <BookshelvesHolding :copies="copies" />

      <div
        class="flex h-11 rounded-xl border-[1.5px] border-border bg-surface p-0.5"
      >
        <label
          v-for="each in kinds"
          :key="each"
          class="flex flex-1 items-center justify-center rounded-[10px] text-button text-muted has-checked:bg-accent has-checked:text-white"
        >
          <input
            v-model="kind"
            type="radio"
            name="kind"
            :value="each"
            class="sr-only"
          />
          {{ t(`isbn.form.kind.${each}`) }}
        </label>
      </div>

      <template v-for="field in fields" :key="field">
        <div class="flex flex-col gap-2">
          <label class="text-label text-muted" :for="`${id}-${field}`">
            {{ t(`isbn.form.${field}`) }}
          </label>
          <input
            :id="`${id}-${field}`"
            v-model="values[field]"
            type="text"
            :inputmode="numeric.has(field) ? 'numeric' : undefined"
            class="h-[50px] w-full rounded-xl border-[1.5px] border-border bg-surface px-3.5 text-field"
          />
        </div>
        <div
          v-if="field === 'volume' && edition?.authors.length"
          class="flex flex-col gap-2"
        >
          <p class="text-label text-muted">{{ t("isbn.form.authors") }}</p>
          <AuthorLines :authors="edition.authors" :kind="edition.kind" />
        </div>
      </template>

      <div class="flex flex-col gap-2">
        <label class="text-label text-muted" :for="`${id}-summary`">
          {{ t("isbn.form.summary") }}
        </label>
        <textarea
          :id="`${id}-summary`"
          v-model="summary"
          rows="3"
          class="field-sizing-content min-h-[calc(3lh+27px)] w-full resize-none rounded-xl border-[1.5px] border-border bg-surface px-3.5 py-3 text-field"
        ></textarea>
      </div>
    </form>
    <DockedAdd :adding="adding" :failed="failed" @add="add" />
  </div>
</template>
