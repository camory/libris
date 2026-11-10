<script setup lang="ts">
import { CircleAlert, Pencil, ScanBarcode, Search, X } from "@lucide/vue";
import {
  computed,
  inject,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  shallowRef,
  useTemplateRef,
} from "vue";
import { useI18n } from "vue-i18n";
import { barcodeScannerKey } from "../../../application/BarcodeScanner";
import { authorsApiKey } from "../../../application/AuthorsApi";
import { beeperKey } from "../../../application/Beeper";
import { beepSettingKey } from "../../../application/BeepSetting";
import { bookshelfApiKey } from "../../../application/BookshelfApi";
import { copiesApiKey } from "../../../application/CopiesApi";
import { isbnApiKey } from "../../../application/IsbnApi";
import { meApiKey } from "../../../application/MeApi";
import { seriesApiKey } from "../../../application/SeriesApi";
import { useAddBookToBookshelf } from "../../../application/useAddBookToBookshelf";
import { useBeepAtEachRead } from "../../../application/useBeepAtEachRead";
import { useListAuthorNames } from "../../../application/useListAuthorNames";
import { useListSeriesNames } from "../../../application/useListSeriesNames";
import { useRemoveCopy } from "../../../application/useRemoveCopy";
import type { Copy } from "../../../domain/Copy";
import type { CoverCandidate } from "../../../domain/Cover";
import { Isbn } from "../../../domain/Isbn";
import type { SourceEdition } from "../../../domain/SourceEdition";
import BusySpinner from "../../components/BusySpinner.vue";
import DockedAdd from "../../components/DockedAdd.vue";
import EditionForm from "../../components/EditionForm.vue";
import IconButton from "../../components/IconButton.vue";
import SourceEditionCard from "../../components/SourceEditionCard.vue";
import SourceEditionCardSkeleton from "../../components/SourceEditionCardSkeleton.vue";

const { t } = useI18n();
const isbnApi = inject(isbnApiKey)!;
const barcodeScanner = inject(barcodeScannerKey)!;
const beeper = inject(beeperKey)!;
const beepSetting = inject(beepSettingKey)!;
const { beep, tap } = useBeepAtEachRead(beeper, beepSetting);
const meApi = inject(meApiKey)!;
const bookshelfApi = inject(bookshelfApiKey)!;
const copiesApi = inject(copiesApiKey)!;
const { names: seriesNames, list: listSeriesNames } = useListSeriesNames(
  inject(seriesApiKey)!,
);
const { names: authorNames, list: listAuthorNames } = useListAuthorNames(
  inject(authorsApiKey)!,
);

const typed = ref("");
const message = ref<string>();
const refused = computed(() => message.value === "isbn.invalid");
const edition = ref<SourceEdition>();
const unknownIsbn13 = ref<string>();
const held = ref(false);
const editing = ref(false);
const copies = ref<Copy[]>([]);
const covers = ref<CoverCandidate[]>([]);
const coverSource = ref<string | null>(null);
const sent = ref<SourceEdition>();
const sentCoverSource = ref<string | null>(null);
const sentCovers = computed(() =>
  covers.value.filter(({ source }) => source === sentCoverSource.value),
);
const shownIsbn13 = computed(
  () => edition.value?.isbn13 ?? unknownIsbn13.value,
);
const searching = ref(false);
const addBookToBookshelf = shallowRef(
  useAddBookToBookshelf(meApi, bookshelfApi),
);
const addState = computed(() => addBookToBookshelf.value.state.value);
const adding = computed(() => addState.value.status === "adding");
const failed = computed(() => addState.value.status === "notAdded");
const shownCopies = computed(() =>
  addState.value.status === "added"
    ? [...copies.value, addState.value.copy]
    : copies.value,
);
let removedId: string | null = null;
const removeCopyFromCard = shallowRef(copyRemoval());
const removalState = computed(() => removeCopyFromCard.value.state.value);
const removing = computed(() => removalState.value.status === "removing");
const removalFailed = computed(
  () => removalState.value.status === "unavailable",
);
const scanning = ref(false);
const answerShown = computed(() => {
  if (scanning.value) {
    return null;
  }
  if (addState.value.status === "added" && sent.value) {
    return "added";
  }
  return editing.value || !edition.value ? "form" : "card";
});
const canScan = ref(false);
const camera = useTemplateRef<HTMLVideoElement>("camera");

onMounted(async () => {
  canScan.value = await barcodeScanner.isAvailable();
  if (canScan.value) {
    await openCamera();
  }
});

onBeforeUnmount(() => barcodeScanner.stop());

async function openCamera() {
  beeper.ready();
  scanning.value = true;
  await nextTick();
  const scanned = await barcodeScanner.read(camera.value!);
  scanning.value = false;
  if (scanned !== null) {
    beep();
    typed.value = scanned;
    await search();
  }
}

async function toggleCamera() {
  if (scanning.value) {
    barcodeScanner.stop();
    return;
  }
  await openCamera();
}

async function search() {
  if (scanning.value) {
    barcodeScanner.stop();
  }
  edition.value = undefined;
  unknownIsbn13.value = undefined;
  copies.value = [];
  covers.value = [];
  coverSource.value = null;
  addBookToBookshelf.value = useAddBookToBookshelf(meApi, bookshelfApi);
  removeCopyFromCard.value = copyRemoval();
  sent.value = undefined;
  editing.value = false;
  message.value = undefined;
  const isbn13 = Isbn.of(typed.value)?.digits ?? null;
  if (isbn13 === null) {
    message.value = "isbn.invalid";
    return;
  }
  void listSeriesNames();
  void listAuthorNames();
  searching.value = true;
  const answer = await isbnApi.lookUp(isbn13);
  searching.value = false;
  if (answer.outcome === "found") {
    edition.value = answer.edition;
    held.value = answer.id !== null;
    copies.value = answer.copies;
    covers.value = answer.covers;
  } else if (answer.type === "/problems/not-found") {
    unknownIsbn13.value = isbn13;
  } else {
    message.value = "isbn.error";
  }
}

function copyRemoval() {
  const removal = useRemoveCopy(copiesApi, async () => {
    if (removeCopyFromCard.value === removal) dropRemovedCopy();
  });
  return removal;
}

function dropRemovedCopy() {
  copies.value = shownCopies.value.filter(({ id }) => id !== removedId);
  if (addState.value.status === "added" && sent.value) {
    edition.value = sent.value;
    covers.value = sentCovers.value;
    held.value = true;
    editing.value = false;
  }
  if (!adding.value) {
    addBookToBookshelf.value = useAddBookToBookshelf(meApi, bookshelfApi);
  }
}

async function removeCopy(copyId: string) {
  removedId = copyId;
  await removeCopyFromCard.value.remove(copyId);
}

async function send(edition: SourceEdition) {
  sent.value = edition;
  sentCoverSource.value = coverSource.value;
  await addBookToBookshelf.value.add(edition, sentCoverSource.value);
}
</script>

<template>
  <main class="mx-auto flex min-h-full w-full max-w-120 flex-col px-5 pb-5">
    <header class="pt-5 pb-3">
      <h1 class="text-page-title" @click="tap($event.timeStamp)">
        {{ t("isbn.title") }}
      </h1>
      <p class="text-body text-muted">{{ t("isbn.hint") }}</p>
    </header>

    <div class="flex flex-col gap-2">
      <label class="text-label text-muted" for="isbn">
        {{ t("isbn.label") }}
      </label>
      <div class="flex gap-2">
        <div class="relative min-w-0 flex-1">
          <input
            id="isbn"
            v-model="typed"
            type="text"
            inputmode="numeric"
            :placeholder="t('isbn.placeholder')"
            class="h-[50px] w-full rounded-xl border-[1.5px] bg-surface pr-13 pl-3.5 text-field tabular-nums outline-none placeholder:text-muted"
            :class="
              refused ? 'border-danger' : 'border-border focus:border-accent'
            "
          />
          <IconButton
            v-if="canScan"
            :label="scanning ? t('isbn.closeCamera') : t('isbn.scan')"
            class="absolute top-1/2 right-[3px] -translate-y-1/2"
            @click="toggleCamera"
          >
            <X v-if="scanning" :size="22" class="shrink-0" />
            <ScanBarcode v-else :size="22" class="shrink-0" />
          </IconButton>
        </div>
        <button
          type="button"
          :disabled="searching"
          :aria-label="searching ? t('isbn.searching') : t('isbn.search')"
          class="flex size-[50px] shrink-0 items-center justify-center rounded-xl bg-accent text-white active:bg-accent-pressed disabled:opacity-70"
          @click="search"
        >
          <BusySpinner v-if="searching" class="size-5.5" />
          <Search v-else :size="22" class="shrink-0" />
        </button>
      </div>
    </div>

    <div
      v-if="scanning"
      class="relative mt-5 min-h-0 flex-1 overflow-hidden rounded-[14px] bg-border"
    >
      <video
        ref="camera"
        muted
        playsinline
        class="absolute inset-0 size-full object-cover"
      ></video>
      <div aria-hidden="true" class="pointer-events-none absolute inset-6">
        <span
          class="absolute top-0 left-0 size-8 rounded-tl-[14px] border-t-2 border-l-2 border-accent"
        ></span>
        <span
          class="absolute top-0 right-0 size-8 rounded-tr-[14px] border-t-2 border-r-2 border-accent"
        ></span>
        <span
          class="absolute bottom-0 left-0 size-8 rounded-bl-[14px] border-b-2 border-l-2 border-accent"
        ></span>
        <span
          class="absolute right-0 bottom-0 size-8 rounded-br-[14px] border-r-2 border-b-2 border-accent"
        ></span>
        <span class="absolute top-1/2 right-0 left-0 h-0.5 bg-accent"></span>
      </div>
    </div>

    <SourceEditionCardSkeleton v-if="searching" class="mt-5" />

    <div
      v-else-if="shownIsbn13"
      class="flex flex-col"
      :class="{ 'mt-5': !scanning }"
    >
      <KeepAlive :max="1">
        <SourceEditionCard
          v-if="answerShown === 'added' && sent"
          :edition="sent"
          :held="held"
          :copies="shownCopies"
          :covers="sentCovers"
          :removing="removing"
          :removal-failed="removalFailed"
          @remove="removeCopy"
        />
        <EditionForm
          v-else-if="answerShown === 'form'"
          :isbn13="shownIsbn13"
          :edition="edition"
          :copies="shownCopies"
          :covers="covers"
          :held="held"
          action="add"
          :shown-source="coverSource"
          :adding="adding"
          :failed="failed"
          :series-names="seriesNames"
          :author-names="authorNames"
          @cover-source="coverSource = $event"
          @add="send($event)"
        >
          <template v-if="editing" #corner>
            <IconButton
              :label="t('isbn.cancelEdit')"
              :disabled="adding"
              @click="editing = false"
            >
              <X :size="22" class="shrink-0" />
            </IconButton>
          </template>
        </EditionForm>
        <SourceEditionCard
          v-else-if="answerShown === 'card' && edition"
          :edition="edition"
          :held="held"
          :copies="shownCopies"
          :covers="covers"
          :shown-source="coverSource"
          :disabled="adding"
          :removing="removing"
          :removal-failed="removalFailed"
          @cover-source="coverSource = $event"
          @remove="removeCopy"
        >
          <template v-if="!held || shownCopies.length === 0" #corner>
            <IconButton
              :label="t('isbn.edit')"
              :disabled="adding"
              @click="editing = true"
            >
              <Pencil :size="22" class="shrink-0" />
            </IconButton>
          </template>
        </SourceEditionCard>
      </KeepAlive>
      <DockedAdd
        v-if="answerShown === 'card' && edition"
        action="add"
        :adding="adding"
        :failed="failed"
        @add="send(edition)"
      />
    </div>

    <p v-if="message" class="mt-5 flex items-start gap-2 text-body text-danger">
      <CircleAlert :size="22" class="shrink-0" />
      <span>{{ t(message) }}</span>
    </p>
  </main>
</template>
