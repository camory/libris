<script setup lang="ts">
import { CircleAlert } from "@lucide/vue";
import { computed, inject, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { catalogueApiKey } from "../../../application/CatalogueApi";
import { copiesApiKey } from "../../../application/CopiesApi";
import { useFindCatalogueEdition } from "../../../application/useFindCatalogueEdition";
import { useRemoveCopy } from "../../../application/useRemoveCopy";
import type { CoverCandidate } from "../../../domain/Cover";
import HeldEditionCardSkeleton from "../../components/HeldEditionCardSkeleton.vue";
import SourceEditionCard from "../../components/SourceEditionCard.vue";

const props = defineProps<{ id: string }>();

const { t } = useI18n();
const router = useRouter();
const { state, find, refresh } = useFindCatalogueEdition(
  inject(catalogueApiKey)!,
);
let removedId: string | null = null;
const { state: removal, remove } = useRemoveCopy(
  inject(copiesApiKey)!,
  afterRemoval,
);

async function removeCopy(copyId: string) {
  removedId = copyId;
  await remove(copyId);
}

async function afterRemoval() {
  if (copiesLeft().length === 0) await router.replace("/catalogue");
  else await refresh(props.id);
}

function copiesLeft() {
  return state.value.status === "found"
    ? state.value.book.copies.filter((copy) => copy.id !== removedId)
    : [];
}

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
      :removing="removal.status === 'removing'"
      :removal-failed="removal.status === 'unavailable'"
      @remove="removeCopy"
    />
    <p v-else class="flex items-start gap-2 text-body text-danger">
      <CircleAlert :size="22" class="shrink-0" />
      <span>{{ failure }}</span>
    </p>
  </main>
</template>
