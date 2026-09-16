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
  <main class="mx-auto w-full max-w-120 px-5 pb-5">
    <header class="pt-5 pb-3">
      <h1 class="text-page-title">{{ t("home.title") }}</h1>
      <p v-if="reader" class="text-body text-muted">
        {{ t("home.greeting", { name: reader.displayName }) }}
      </p>
    </header>

    <RouterLink to="/isbn" class="text-body text-accent">
      {{ t("home.add") }}
    </RouterLink>
  </main>
</template>
