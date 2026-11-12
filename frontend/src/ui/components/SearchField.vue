<script setup lang="ts">
import { Search } from "@lucide/vue";
import { useI18n } from "vue-i18n";

const text = defineModel<string>({ required: true });

const { t } = useI18n();
</script>

<template>
  <div class="relative">
    <Search
      :size="22"
      class="pointer-events-none absolute top-1/2 left-3.5 -translate-y-1/2 text-muted"
    />
    <input
      v-model="text"
      type="search"
      :aria-label="t('catalogue.search.label')"
      :placeholder="t('catalogue.search.placeholder')"
      class="h-[50px] w-full rounded-xl border-[1.5px] border-border bg-surface pr-3.5 pl-11 text-field outline-none placeholder:text-muted focus:border-accent"
    />
  </div>
</template>
