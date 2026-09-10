<script setup lang="ts">
import { inject, ref } from "vue";
import { useI18n } from "vue-i18n";
import { meApiKey } from "../../../application/MeApi";
import type { Reader } from "../../../domain/Reader";

const { t } = useI18n();
const reader = ref<Reader>();

const meApi = inject(meApiKey)!;

meApi.currentReader().then((current) => {
  reader.value = current;
});
</script>

<template>
  <main class="p-4">
    <h1 class="text-2xl font-bold">{{ t("home.title") }}</h1>
    <p v-if="reader">{{ t("home.greeting", { name: reader.displayName }) }}</p>
  </main>
</template>
