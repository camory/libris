<script setup lang="ts">
import { ChevronDown } from "@lucide/vue";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import type { KnownReader } from "../../domain/KnownReader";

defineProps<{
  readers: KnownReader[];
  adding: boolean;
}>();

const { t } = useI18n();
const readerId = ref("");
</script>

<template>
  <div class="flex items-center gap-2 py-1.5 pr-1.5 pl-3.5">
    <div class="flex min-w-0 flex-1 items-center gap-2">
      <select
        v-model="readerId"
        :aria-label="t('bookshelf.members.reader')"
        class="field-sizing-content min-w-0 appearance-none bg-transparent text-field outline-none"
        :class="readerId === '' ? 'text-muted' : 'text-text'"
      >
        <option value="">{{ t("bookshelf.members.choose") }}</option>
        <option v-for="reader in readers" :key="reader.id" :value="reader.id">
          {{ reader.displayName }}
        </option>
      </select>
      <ChevronDown class="shrink-0 text-muted" />
    </div>
  </div>
</template>
