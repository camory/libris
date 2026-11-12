<script setup lang="ts">
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>

<template>
  <div class="relative">
    <input
      type="search"
      :aria-label="t('catalogue.search.label')"
      class="h-[50px] w-full rounded-xl border-[1.5px] border-border bg-surface px-3.5 text-field outline-none focus:border-accent"
    />
  </div>
</template>
