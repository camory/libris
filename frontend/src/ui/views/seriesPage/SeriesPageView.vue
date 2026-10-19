<script setup lang="ts">
import { inject } from "vue";
import { seriesApiKey } from "../../../application/SeriesApi";
import { useFindCatalogueSeries } from "../../../application/useFindCatalogueSeries";
import CatalogueRowSkeleton from "../../components/CatalogueRowSkeleton.vue";
import FieldRowSkeleton from "../../components/FieldRowSkeleton.vue";
import PageHintSkeleton from "../../components/PageHintSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";

const props = defineProps<{ id: string }>();

const { state, find } = useFindCatalogueSeries(inject(seriesApiKey)!);

void find(props.id);
</script>

<template>
  <main class="mx-auto w-full max-w-120 px-5 pb-5">
    <template v-if="state.status === 'loading'">
      <header class="pt-5 pb-3">
        <PageTitleSkeleton />
        <PageHintSkeleton />
      </header>
      <div
        class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
      >
        <FieldRowSkeleton v-for="row in 2" :key="row" />
      </div>
      <div
        class="mt-5 divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
      >
        <CatalogueRowSkeleton v-for="row in 5" :key="row" />
      </div>
    </template>
  </main>
</template>
