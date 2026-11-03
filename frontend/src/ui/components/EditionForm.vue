<script setup lang="ts">
import { CircleAlert } from "@lucide/vue";
import { computed, reactive, ref, useId } from "vue";
import { useI18n } from "vue-i18n";
import type { Copy } from "../../domain/Copy";
import type { CoverCandidate } from "../../domain/Cover";
import {
  editionOf,
  refusalsOf,
  type EditionDraft,
  type Refusal,
  type Refusals,
} from "../../domain/EditionDraft";
import { languageWord } from "../../domain/Language";
import type { Kind, SourceEdition } from "../../domain/SourceEdition";
import { suggestionsFor, type Suggestion } from "../../domain/Suggestion";
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
  shownSource?: string | null;
  seriesNames: string[];
}>();

const emit = defineEmits<{
  coverSource: [source: string | null];
  add: [edition: SourceEdition];
}>();

defineSlots<{ corner?: () => unknown }>();

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
const seriesFocused = ref(false);
const chosen = ref(false);
const suggestions = computed(() =>
  seriesFocused.value && !chosen.value
    ? suggestionsFor(values.series, props.seriesNames, locale.value)
    : [],
);

function text(value: string | number | null | undefined) {
  return value == null ? "" : String(value);
}

function focused(field: (typeof fields)[number]) {
  if (field !== "series") return;
  seriesFocused.value = true;
  chosen.value = false;
  boxes.series?.scrollIntoView?.({ block: "start" });
}

function left(field: (typeof fields)[number]) {
  if (field === "series") seriesFocused.value = false;
}

function typed(field: (typeof fields)[number]) {
  if (field === "series") chosen.value = false;
}

function choose(name: string) {
  values.series = name;
  chosen.value = true;
}

function nameOf(suggestion: Suggestion) {
  return suggestion.before + suggestion.matched + suggestion.after;
}

function labelOf(field: (typeof fields)[number]) {
  return field === "volume"
    ? `isbn.form.volume.${kind.value}`
    : `isbn.form.${field}`;
}

function messageOf(refusal: Refusal) {
  return refusal === "withoutSeries"
    ? `isbn.form.refusal.withoutSeries.${kind.value}`
    : `isbn.form.refusal.${refusal}`;
}

const id = useId();
const refusals = ref<Refusals>({});
const boxes: Partial<Record<(typeof fields)[number], HTMLInputElement>> = {};

function add() {
  const draft: EditionDraft = {
    kind: kind.value,
    ...values,
    summary: summary.value,
  };
  refusals.value = refusalsOf(draft, locale.value);
  const first = fields.find((field) => refusals.value[field]);
  if (first) {
    boxes[first]?.focus();
    boxes[first]?.scrollIntoView?.({ block: "center" });
    return;
  }
  emit(
    "add",
    editionOf(props.isbn13, answered?.authors ?? [], draft, locale.value),
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
          :disabled="adding"
          :shown-source="shownSource"
          @cover-source="emit('coverSource', $event)"
        />
        <div class="flex min-w-0 flex-1 gap-2">
          <div class="flex min-w-0 flex-1 flex-col gap-1.5">
            <p class="text-label text-muted">{{ t("isbn.card.isbn") }}</p>
            <p class="text-body tabular-nums">{{ isbn13 }}</p>
          </div>
          <slot name="corner" />
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
            :disabled="adding"
            class="sr-only"
          />
          {{ t(`isbn.form.kind.${each}`) }}
        </label>
      </div>

      <template v-for="field in fields" :key="field">
        <div class="flex flex-col gap-2">
          <label class="text-label text-muted" :for="`${id}-${field}`">
            {{ t(labelOf(field)) }}
          </label>
          <input
            :id="`${id}-${field}`"
            :ref="(box) => (boxes[field] = box as HTMLInputElement)"
            v-model="values[field]"
            type="text"
            :disabled="adding"
            :inputmode="numeric.has(field) ? 'numeric' : undefined"
            :aria-invalid="refusals[field] ? 'true' : undefined"
            @focus="focused(field)"
            @blur="left(field)"
            @input="typed(field)"
            class="h-[50px] w-full rounded-xl border-[1.5px] border-border bg-surface px-3.5 text-field aria-invalid:border-danger"
          />
          <div
            v-if="field === 'series' && suggestions.length"
            class="-mt-1 flex flex-col divide-y divide-border rounded-xl border-[1.5px] border-border bg-surface"
          >
            <button
              v-for="suggestion in suggestions"
              :key="nameOf(suggestion)"
              type="button"
              @mousedown.prevent
              @click="choose(nameOf(suggestion))"
              class="mx-3.5 h-11 text-left text-field"
            >
              <span>{{ suggestion.before }}</span>
              <span class="font-semibold">{{ suggestion.matched }}</span>
              <span>{{ suggestion.after }}</span>
            </button>
          </div>
          <p
            v-if="refusals[field]"
            class="flex items-start gap-2 text-body text-danger"
          >
            <CircleAlert :size="22" class="shrink-0" />
            <span>{{ t(messageOf(refusals[field])) }}</span>
          </p>
        </div>
        <div
          v-if="field === 'volume' && edition?.authors.length"
          class="flex flex-col gap-2"
        >
          <p class="text-label text-muted">{{ t("isbn.form.authors") }}</p>
          <AuthorLines :authors="edition.authors" :kind="kind" />
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
          :disabled="adding"
          class="field-sizing-content min-h-[calc(3lh+27px)] w-full resize-none rounded-xl border-[1.5px] border-border bg-surface px-3.5 py-3 text-field"
        ></textarea>
      </div>
    </form>
    <DockedAdd :adding="adding" :failed="failed" @add="add" />
  </div>
</template>
