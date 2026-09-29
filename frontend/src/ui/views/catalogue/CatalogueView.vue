<script setup lang="ts">
import { inject, onUnmounted, useTemplateRef, watch } from "vue";
import { useI18n } from "vue-i18n";
import { catalogueApiKey } from "../../../application/CatalogueApi";
import { useBrowseCatalogue } from "../../../application/useBrowseCatalogue";
import CatalogueRow from "../../components/CatalogueRow.vue";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
import IconAlert from "../../components/icons/IconAlert.vue";
import IconBook from "../../components/icons/IconBook.vue";

const { t } = useI18n();
const { state, browse } = useBrowseCatalogue(inject(catalogueApiKey)!);

const rows = useTemplateRef("rows");
const observer = new IntersectionObserver((entries) => {
  if (entries.some((entry) => entry.isIntersecting)) void browse();
});
let lastRow: Element | null = null;

watch(
  state,
  () => {
    const row = rows.value?.lastElementChild ?? null;
    if (row === lastRow) return;
    if (lastRow !== null) observer.unobserve(lastRow);
    if (row !== null) observer.observe(row);
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
      <IconBook class="text-muted opacity-60" />
      <p class="text-lead text-muted">{{ t("catalogue.empty") }}</p>
    </div>
    <template v-else>
      <div
        v-if="state.books.length > 0"
        class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
      >
        <ul ref="rows" class="divide-y divide-border">
          <li v-for="book in state.books" :key="book.id">
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
        <IconAlert />
        <span>{{ t("catalogue.error") }}</span>
      </p>
    </template>
  </main>
</template>
