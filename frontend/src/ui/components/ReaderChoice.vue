<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { KnownReader } from "../../domain/KnownReader";

defineProps<{
  readers: KnownReader[];
  adding: boolean;
}>();

const { t } = useI18n();
</script>

<template>
  <section>
    <div class="flex min-h-11 items-center justify-between pb-2">
      <h2 class="text-label text-muted">
        {{ t("bookshelf.members.choose") }}
      </h2>
    </div>
    <div class="overflow-hidden rounded-[14px] border border-border bg-surface">
      <ul
        :aria-label="t('bookshelf.members.choose')"
        class="max-h-[313px] divide-y divide-border overflow-y-auto"
      >
        <li v-for="reader in readers" :key="reader.id">
          <button
            type="button"
            class="flex min-h-14 w-full items-center px-3.5 text-left text-body text-text"
          >
            {{ reader.displayName }}
          </button>
        </li>
      </ul>
    </div>
  </section>
</template>
