<script setup lang="ts">
import { CircleAlert } from "@lucide/vue";
import { computed, inject, watch } from "vue";
import { useI18n } from "vue-i18n";
import { catalogueApiKey } from "../../../application/CatalogueApi";
import { useFindCatalogueEdition } from "../../../application/useFindCatalogueEdition";
import type { CoverCandidate } from "../../../domain/Cover";
import HeldEditionCardSkeleton from "../../components/HeldEditionCardSkeleton.vue";
import SourceEditionCard from "../../components/SourceEditionCard.vue";

const props = defineProps<{ id: string }>();

const { t } = useI18n();
const { state, find } = useFindCatalogueEdition(inject(catalogueApiKey)!);

watch(
  () => props.id,
  (id) => void find(id),
  { immediate: true },
);

const covers = computed<CoverCandidate[]>(() => {
  const coverUrl = state.value.status === "found" && state.value.book.coverUrl;
  return coverUrl ? [{ source: "Libris", url: coverUrl }] : [];
});

const failure = computed(() =>
  state.value.status === "unavailable"
    ? t("editionPage.error")
    : t("editionPage.notFound"),
);
</script>

<template>
  <main class="mx-auto w-full max-w-120 px-5 pt-5 pb-5">
    <HeldEditionCardSkeleton v-if="state.status === 'loading'" />
    <SourceEditionCard
      v-else-if="state.status === 'found'"
      :edition="state.book"
      :copies="state.book.copies"
      :covers="covers"
      held
      page-title
    />
    <p v-else class="flex items-start gap-2 text-body text-danger">
      <CircleAlert :size="22" class="shrink-0" />
      <span>{{ failure }}</span>
    </p>
  </main>
</template>
