<script setup lang="ts">
import { inject } from "vue";
import { useI18n } from "vue-i18n";
import { catalogueApiKey } from "../../../application/CatalogueApi";
import { useBrowseCatalogue } from "../../../application/useBrowseCatalogue";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
import IconBook from "../../components/icons/IconBook.vue";

const { t } = useI18n();
const { state, browse } = useBrowseCatalogue(inject(catalogueApiKey)!);

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
  </main>
</template>
