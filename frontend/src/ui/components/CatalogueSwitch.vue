<script setup lang="ts">
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>

<template>
  <nav
    :aria-label="t('catalogue.switch.name')"
    class="grid h-11 w-full grid-cols-2 rounded-xl border-[1.5px] border-border bg-surface p-0.5 text-button text-muted"
  >
    <RouterLink
      to="/catalogue"
      class="flex items-center justify-center rounded-[10px]"
      exact-active-class="bg-accent text-white"
    >
      {{ t("catalogue.switch.books") }}
    </RouterLink>
    <RouterLink
      to="/catalogue/series"
      class="flex items-center justify-center rounded-[10px]"
      exact-active-class="bg-accent text-white"
    >
      {{ t("catalogue.switch.series") }}
    </RouterLink>
  </nav>
</template>
