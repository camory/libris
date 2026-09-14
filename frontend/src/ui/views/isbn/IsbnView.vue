<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { isbn13Of } from "../../../domain/Isbn13";
import IconAlert from "../../components/icons/IconAlert.vue";

const { t } = useI18n();

const typed = ref("");
const message = ref<string>();

function search() {
  const isbn13 = isbn13Of(typed.value);
  if (isbn13 === null) {
    message.value = "isbn.invalid";
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

    <p v-if="message" class="mt-5 flex items-start gap-2 text-body text-danger">
      <IconAlert />
      <span>{{ t(message) }}</span>
    </p>
  </main>
</template>
