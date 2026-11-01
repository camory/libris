<script setup lang="ts">
import { CircleAlert, ScanBarcode, Search, X } from "@lucide/vue";
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
import { bookshelfApiKey } from "../../../application/BookshelfApi";
import { isbnApiKey } from "../../../application/IsbnApi";
import { meApiKey } from "../../../application/MeApi";
import { useAddBookToBookshelf } from "../../../application/useAddBookToBookshelf";
import type { Copy } from "../../../domain/Copy";
import type { CoverCandidate } from "../../../domain/Cover";
import { Isbn } from "../../../domain/Isbn";
import type { SourceEdition } from "../../../domain/SourceEdition";
import BusySpinner from "../../components/BusySpinner.vue";
import DockedAdd from "../../components/DockedAdd.vue";
import EditionForm from "../../components/EditionForm.vue";
import SourceEditionCard from "../../components/SourceEditionCard.vue";
import SourceEditionCardSkeleton from "../../components/SourceEditionCardSkeleton.vue";

const { t } = useI18n();
const isbnApi = inject(isbnApiKey)!;
const barcodeScanner = inject(barcodeScannerKey)!;
const meApi = inject(meApiKey)!;
const bookshelfApi = inject(bookshelfApiKey)!;

const typed = ref("");
const message = ref<string>();
const refused = computed(() => message.value === "isbn.invalid");
const edition = ref<SourceEdition>();
const unknownIsbn13 = ref<string>();
const held = ref(false);
const copies = ref<Copy[]>([]);
const covers = ref<CoverCandidate[]>([]);
const coverSource = ref<string | null>(null);
const sent = ref<SourceEdition>();
const shownIsbn13 = computed(
  () => edition.value?.isbn13 ?? unknownIsbn13.value,
);
const searching = ref(false);
const addBookToBookshelf = shallowRef(
  useAddBookToBookshelf(meApi, bookshelfApi),
);
const addState = computed(() => addBookToBookshelf.value.state.value);
const shownCopies = computed(() =>
  addState.value.status === "added"
    ? [...copies.value, addState.value.copy]
    : copies.value,
);
const scanning = ref(false);
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
  scanning.value = true;
  await nextTick();
  const scanned = await barcodeScanner.read(camera.value!);
  scanning.value = false;
  if (scanned !== null) {
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
  edition.value = undefined;
  unknownIsbn13.value = undefined;
  copies.value = [];
  covers.value = [];
  addBookToBookshelf.value = useAddBookToBookshelf(meApi, bookshelfApi);
  sent.value = undefined;
  message.value = undefined;
  const isbn13 = Isbn.of(typed.value)?.digits ?? null;
  if (isbn13 === null) {
    message.value = "isbn.invalid";
    return;
  }
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

async function send(edition: SourceEdition) {
  sent.value = edition;
  await addBookToBookshelf.value.add(edition, coverSource.value);
}
</script>

<template>
  <main class="mx-auto flex min-h-full w-full max-w-120 flex-col px-5 pb-5">
    <header class="pt-5 pb-3">
      <h1 class="text-page-title">{{ t("isbn.title") }}</h1>
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
            class="h-[50px] w-full rounded-xl border-[1.5px] bg-surface pr-13 pl-3.5 text-field tabular-nums placeholder:text-muted"
            :class="refused ? 'border-danger' : 'border-border'"
          />
          <button
            v-if="canScan"
            type="button"
            :aria-label="scanning ? t('isbn.closeCamera') : t('isbn.scan')"
            class="absolute top-1/2 right-[3px] flex size-11 -translate-y-1/2 items-center justify-center rounded-[10px] text-accent"
            @click="toggleCamera"
          >
            <X v-if="scanning" :size="22" class="shrink-0" />
            <ScanBarcode v-else :size="22" class="shrink-0" />
          </button>
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

    <div v-else-if="shownIsbn13" class="mt-5 flex flex-col">
      <SourceEditionCard
        v-if="addState.status === 'added' && sent"
        :edition="sent"
        :held="held"
        :copies="shownCopies"
        :covers="covers"
      />
      <template v-else-if="held && edition">
        <SourceEditionCard
          :edition="edition"
          :held="held"
          :copies="shownCopies"
          :covers="covers"
          @cover-source="coverSource = $event"
        />
        <DockedAdd
          :adding="addState.status === 'adding'"
          :failed="addState.status === 'notAdded'"
          @add="send(edition)"
        />
      </template>
      <EditionForm
        v-else
        :isbn13="shownIsbn13"
        :edition="edition"
        :copies="shownCopies"
        :covers="covers"
        :adding="addState.status === 'adding'"
        :failed="addState.status === 'notAdded'"
        @cover-source="coverSource = $event"
        @add="send($event)"
      />
    </div>

    <p v-if="message" class="mt-5 flex items-start gap-2 text-body text-danger">
      <CircleAlert :size="22" class="shrink-0" />
      <span>{{ t(message) }}</span>
    </p>
  </main>
</template>
