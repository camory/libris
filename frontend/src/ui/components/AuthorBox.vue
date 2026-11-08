<script setup lang="ts">
import { ChevronDown } from "@lucide/vue";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { AuthorRole, Kind } from "../../domain/SourceEdition";

const props = defineProps<{ roles: AuthorRole[]; kind: Kind }>();

const name = defineModel<string>("name", { required: true });

const { t } = useI18n();

const words = computed(() =>
  props.roles.map((role) => t(`role.${props.kind}.${role}`)).join(", "),
);
const label = computed(() =>
  [t("isbn.form.authors.roles"), words.value].filter(Boolean).join(" "),
);
</script>

<template>
  <div class="rounded-xl border-[1.5px] border-border bg-surface">
    <input
      v-model="name"
      type="text"
      :aria-label="t('isbn.form.authors.name')"
      class="h-12 w-full bg-transparent pr-0.75 pl-3.5 text-field"
    />
    <div class="mx-3.5 border-t border-border"></div>
    <button
      type="button"
      aria-expanded="false"
      :aria-label="label"
      class="flex min-h-11 w-full items-center gap-2 pr-0.75 pl-3.5 text-left text-body"
    >
      <span class="flex-1">{{ words }}</span>
      <span class="flex size-11 shrink-0 items-center justify-center">
        <ChevronDown :size="22" class="text-muted" />
      </span>
    </button>
  </div>
</template>
