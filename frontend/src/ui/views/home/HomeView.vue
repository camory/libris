<script setup lang="ts">
import { inject, ref } from "vue";
import { useI18n } from "vue-i18n";
import { meApiKey } from "../../../application/MeApi";
import { revisionKey } from "../../../application/Revision";
import type { Reader } from "../../../domain/Reader";

const { t } = useI18n();
const reader = ref<Reader>();

const meApi = inject(meApiKey)!;
const revision = inject(revisionKey)!;

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

    <p class="mt-5 text-label text-muted">{{ revision }}</p>
  </main>
</template>
