<script setup lang="ts">
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>

<template>
  <main class="mx-auto w-full max-w-120">
    <header class="px-5 pt-5 pb-3">
      <h1 class="text-page-title">{{ t("isbn.title") }}</h1>
      <p class="text-body text-muted">{{ t("isbn.hint") }}</p>
    </header>
  </main>
</template>
