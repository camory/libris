<script setup lang="ts">
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>

<template>
  <nav>
    <RouterLink to="/">{{ t("tabs.home") }}</RouterLink>
    <RouterLink to="/isbn">{{ t("tabs.add") }}</RouterLink>
  </nav>
</template>
