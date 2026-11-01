<script setup lang="ts">
import { BookOpen } from "@lucide/vue";
import { reactive, ref, useId } from "vue";
import { useI18n } from "vue-i18n";
import type { Kind } from "../../domain/SourceEdition";

defineProps<{ isbn13: string }>();

const { t } = useI18n();

const kinds: Kind[] = ["BOOK", "BD", "MANGA"];
const kind = ref<Kind>("BOOK");

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
  "summary",
] as const;
const numeric = new Set<string>(["volume", "year", "pages"]);
const values = reactive(
  Object.fromEntries(fields.map((field) => [field, ""])) as Record<
    (typeof fields)[number],
    string
  >,
);
const id = useId();
</script>

<template>
  <form
    class="flex flex-col gap-3.5 rounded-[14px] border border-border bg-surface p-4"
  >
    <div class="flex gap-3.5">
      <div
        class="flex h-[149px] w-24 shrink-0 items-center justify-center rounded-md bg-border"
      >
        <BookOpen :size="48" class="shrink-0 text-muted opacity-60" />
      </div>
      <div class="flex min-w-0 flex-col gap-1.5">
        <p class="text-label text-muted">{{ t("isbn.card.isbn") }}</p>
        <p class="text-body tabular-nums">{{ isbn13 }}</p>
      </div>
    </div>

    <div class="flex items-center gap-2 text-body font-semibold">
      <BookOpen :size="22" class="shrink-0" />
      <p>{{ t("isbn.card.noCopy") }}</p>
    </div>

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

    <div v-for="field in fields" :key="field" class="flex flex-col gap-2">
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
  </form>
</template>
