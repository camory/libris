<script setup lang="ts">
import { CircleAlert, Pencil, X } from "@lucide/vue";
import { computed, inject, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { authorsApiKey } from "../../../application/AuthorsApi";
import { catalogueApiKey } from "../../../application/CatalogueApi";
import { copiesApiKey } from "../../../application/CopiesApi";
import { editionsApiKey } from "../../../application/EditionsApi";
import { seriesApiKey } from "../../../application/SeriesApi";
import { useFindCatalogueEdition } from "../../../application/useFindCatalogueEdition";
import { useListAuthorNames } from "../../../application/useListAuthorNames";
import { useListSeriesNames } from "../../../application/useListSeriesNames";
import { useRemoveCopy } from "../../../application/useRemoveCopy";
import { useSaveEdition } from "../../../application/useSaveEdition";
import type { CoverCandidate } from "../../../domain/Cover";
import type { SourceEdition } from "../../../domain/SourceEdition";
import EditionForm from "../../components/EditionForm.vue";
import HeldEditionCardSkeleton from "../../components/HeldEditionCardSkeleton.vue";
import IconButton from "../../components/IconButton.vue";
import SourceEditionCard from "../../components/SourceEditionCard.vue";

const props = defineProps<{ id: string }>();

const { t } = useI18n();
const router = useRouter();
const { state, find, refresh, show } = useFindCatalogueEdition(
  inject(catalogueApiKey)!,
);
const { names: seriesNames, list: listSeriesNames } = useListSeriesNames(
  inject(seriesApiKey)!,
);
const { names: authorNames, list: listAuthorNames } = useListAuthorNames(
  inject(authorsApiKey)!,
);
const editing = ref(false);
const {
  state: saveState,
  save,
  reset: forgetSave,
} = useSaveEdition(inject(editionsApiKey)!);
const saving = computed(() => saveState.value.status === "saving");
let removedId: string | null = null;
const { state: removal, remove } = useRemoveCopy(
  inject(copiesApiKey)!,
  afterRemoval,
);

async function afterRemoval() {
  if (copiesLeft().length > 0) await refresh(props.id);
  else if (openedFromAnotherScreen()) await back();
  else await router.replace("/catalogue");
}

function copiesLeft() {
  return state.value.status === "found"
    ? state.value.book.copies.filter((copy) => copy.id !== removedId)
    : [];
}

function openedFromAnotherScreen() {
  return Boolean(router.options.history.state.back);
}

function back() {
  return new Promise<void>((resolve) => {
    const stop = router.afterEach(() => {
      stop();
      resolve();
    });
    router.back();
  });
}

async function removeCopy(copyId: string) {
  removedId = copyId;
  await remove(copyId);
}

function edit() {
  forgetSave();
  editing.value = true;
  void listSeriesNames();
  void listAuthorNames();
}

async function saveEdits(edition: SourceEdition) {
  await save(props.id, edition);
  if (saveState.value.status !== "saved") return;
  show(saveState.value.book);
  editing.value = false;
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
    <template
      v-else-if="state.status === 'found' && editing && state.book.isbn13"
    >
      <h1 class="sr-only">{{ state.book.title }}</h1>
      <EditionForm
        :isbn13="state.book.isbn13"
        :edition="state.book"
        :copies="state.book.copies"
        :covers="covers"
        held
        action="save"
        :adding="saving"
        :failed="saveState.status === 'notSaved'"
        :series-names="seriesNames"
        :author-names="authorNames"
        @add="saveEdits"
      >
        <template #corner>
          <IconButton
            :label="t('isbn.cancelEdit')"
            :disabled="saving"
            @click="editing = false"
          >
            <X :size="22" class="shrink-0" />
          </IconButton>
        </template>
      </EditionForm>
    </template>
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
    >
      <template v-if="state.book.isbn13" #corner>
        <IconButton :label="t('isbn.edit')" @click="edit">
          <Pencil :size="22" class="shrink-0" />
        </IconButton>
      </template>
    </SourceEditionCard>
    <p v-else class="flex items-start gap-2 text-body text-danger">
      <CircleAlert :size="22" class="shrink-0" />
      <span>{{ failure }}</span>
    </p>
  </main>
</template>
