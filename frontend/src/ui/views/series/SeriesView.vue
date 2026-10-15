<script setup lang="ts">
import { useI18n } from "vue-i18n";
import SeriesRowSkeleton from "../../components/SeriesRowSkeleton.vue";

const { t } = useI18n();
</script>

<template>
  <main class="mx-auto flex min-h-full w-full max-w-120 flex-col px-5 pb-5">
    <header class="pt-5 pb-3">
      <h1 class="text-page-title">{{ t("catalogue.title") }}</h1>
      <p class="text-body text-muted">{{ t("catalogue.hint") }}</p>
    </header>

    <div
      class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
    >
      <SeriesRowSkeleton v-for="row in 5" :key="row" />
    </div>
  </main>
</template>
