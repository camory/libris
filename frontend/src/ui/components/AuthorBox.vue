<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { AuthorRole, Kind } from "../../domain/SourceEdition";

defineProps<{ roles: AuthorRole[]; kind: Kind }>();

const name = defineModel<string>("name", { required: true });

const { t } = useI18n();
</script>

<template>
  <div class="rounded-xl border-[1.5px] border-border bg-surface">
    <input
      v-model="name"
      type="text"
      :aria-label="t('isbn.form.authors.name')"
      class="h-12 w-full bg-transparent pr-0.75 pl-3.5 text-field"
    />
  </div>
</template>
