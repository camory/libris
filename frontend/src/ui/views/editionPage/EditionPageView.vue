<script setup lang="ts">
import { inject } from "vue";
import { catalogueApiKey } from "../../../application/CatalogueApi";
import { useFindCatalogueEdition } from "../../../application/useFindCatalogueEdition";
import HeldEditionCardSkeleton from "../../components/HeldEditionCardSkeleton.vue";

const props = defineProps<{ id: string }>();

const { state, find } = useFindCatalogueEdition(inject(catalogueApiKey)!);

void find(props.id);
</script>

<template>
  <main class="mx-auto w-full max-w-120 px-5 pt-5 pb-5">
    <HeldEditionCardSkeleton v-if="state.status === 'loading'" />
  </main>
</template>
