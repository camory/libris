<script setup lang="ts">
import { BookOpen, CircleAlert } from "@lucide/vue";
import { inject, onUnmounted, ref, useTemplateRef, watch } from "vue";
import { useI18n } from "vue-i18n";
import { catalogueApiKey } from "../../../application/CatalogueApi";
import { searchApiKey } from "../../../application/SearchApi";
import { useBrowseCatalogue } from "../../../application/useBrowseCatalogue";
import { useSearchCatalogue } from "../../../application/useSearchCatalogue";
import type { Book } from "../../../domain/Book";
import CatalogueRow from "../../components/CatalogueRow.vue";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
import CatalogueSwitch from "../../components/CatalogueSwitch.vue";
import SearchField from "../../components/SearchField.vue";

const { t } = useI18n();
const { state, browse } = useBrowseCatalogue(inject(catalogueApiKey)!);
const { state: searchState, search } = useSearchCatalogue(
  inject(searchApiKey)!,
);
const searchText = ref("");

function rowsOf(catalogueBooks: Book[]): Book[] {
  if (
    searchState.value.status === "found" ||
    searchState.value.status === "near"
  )
    return searchState.value.books;
  if (searchState.value.status === "nothing") return [];
  return catalogueBooks;
}

const rows = useTemplateRef("rows");
const observer = new IntersectionObserver((entries) => {
  if (entries.some((entry) => entry.isIntersecting)) void browse();
});
let lastRow: Element | null = null;

watch(
  state,
  () => {
    const row = rows.value?.lastElementChild ?? null;
    if (row === null || row === lastRow) return;
    if (lastRow !== null) observer.unobserve(lastRow);
    observer.observe(row);
    lastRow = row;
  },
  { flush: "post" },
);
onUnmounted(() => observer.disconnect());

void browse();
</script>

<template>
  <main class="mx-auto flex min-h-full w-full max-w-120 flex-col px-5 pb-5">
    <header class="pt-5 pb-3">
      <h1 class="text-page-title">{{ t("catalogue.title") }}</h1>
      <p class="text-body text-muted">{{ t("catalogue.hint") }}</p>
    </header>
    <CatalogueSwitch class="mb-5" />
    <SearchField
      v-if="state.status !== 'empty'"
      v-model="searchText"
      class="mb-5"
      @search="search"
    />

    <div
      v-if="state.status === 'loading'"
      class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
    >
      <CatalogueRowSkeleton v-for="row in 5" :key="row" />
    </div>
    <div
      v-else-if="state.status === 'empty'"
      class="flex flex-col items-center gap-3 p-7 text-center"
    >
      <BookOpen :size="48" class="shrink-0 text-muted opacity-60" />
      <p class="text-lead text-muted">{{ t("catalogue.empty") }}</p>
    </div>
    <template v-else>
      <p
        v-if="searchState.status === 'nothing' || searchState.status === 'near'"
        class="text-lead text-muted"
      >
        {{ t("catalogue.search.nothing", { query: searchState.query }) }}
      </p>
      <h2
        v-if="searchState.status === 'near'"
        class="mt-5 pb-2 text-label text-muted"
      >
        {{ t("catalogue.search.near") }}
      </h2>
      <div
        v-if="rowsOf(state.books).length > 0"
        class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
      >
        <ul
          ref="rows"
          :aria-label="
            searchState.status === 'near'
              ? t('catalogue.search.near')
              : undefined
          "
          class="divide-y divide-border"
        >
          <li v-for="book in rowsOf(state.books)" :key="book.id">
            <CatalogueRow :book="book" />
          </li>
        </ul>
        <template v-if="state.status === 'loadingMore'">
          <CatalogueRowSkeleton v-for="row in 2" :key="row" />
        </template>
      </div>
      <p
        v-if="state.status === 'unavailable'"
        class="flex items-start gap-2 text-body text-danger"
        :class="{ 'mt-5': state.books.length > 0 }"
      >
        <CircleAlert :size="22" class="shrink-0" />
        <span>{{ t("catalogue.error") }}</span>
      </p>
    </template>
  </main>
</template>
