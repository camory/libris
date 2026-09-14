<script setup lang="ts">
import { inject, ref } from "vue";
import { useI18n } from "vue-i18n";
import { isbnApiKey } from "../../../application/IsbnApi";
import { isbn13Of } from "../../../domain/Isbn13";
import type { SourceEdition } from "../../../domain/SourceEdition";
import IconAlert from "../../components/icons/IconAlert.vue";

const messages: Record<string, string> = {
  "/problems/not-found": "isbn.unknown",
};

const { t } = useI18n();
const isbnApi = inject(isbnApiKey)!;

const typed = ref("");
const message = ref<string>();
const edition = ref<SourceEdition>();

async function search() {
  const isbn13 = isbn13Of(typed.value);
  if (isbn13 === null) {
    message.value = "isbn.invalid";
    return;
  }
  const answer = await isbnApi.lookUp(isbn13);
  if (answer.outcome === "found") {
    edition.value = answer.edition;
  } else {
    message.value = messages[answer.type];
  }
}
</script>

<template>
  <main class="mx-auto w-full max-w-120 px-5 pb-5">
    <header class="pt-5 pb-3">
      <h1 class="text-page-title">{{ t("isbn.title") }}</h1>
      <p class="text-body text-muted">{{ t("isbn.hint") }}</p>
    </header>

    <div class="flex flex-col gap-2">
      <label class="text-label text-muted" for="isbn">
        {{ t("isbn.label") }}
      </label>
      <input
        id="isbn"
        v-model="typed"
        type="text"
        inputmode="numeric"
        :placeholder="t('isbn.placeholder')"
        class="h-[50px] rounded-xl border-[1.5px] bg-surface px-3.5 text-field tabular-nums"
        :class="message ? 'border-danger' : 'border-border'"
      />
      <button
        type="button"
        class="h-[50px] rounded-xl bg-accent text-button text-white active:bg-accent-pressed"
        @click="search"
      >
        {{ t("isbn.search") }}
      </button>
    </div>

    <p v-if="edition" class="mt-5 text-card-title">{{ edition.title }}</p>

    <p v-if="message" class="mt-5 flex items-start gap-2 text-body text-danger">
      <IconAlert />
      <span>{{ t(message) }}</span>
    </p>
  </main>
</template>
