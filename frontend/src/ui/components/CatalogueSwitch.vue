<script setup lang="ts">
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>

<template>
  <nav :aria-label="t('catalogue.switch.name')" />
</template>
