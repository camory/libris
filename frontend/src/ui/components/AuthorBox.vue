<script setup lang="ts">
import { ChevronDown, Trash2 } from "@lucide/vue";
import { computed, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import type { AuthorRole, Kind } from "../../domain/SourceEdition";
import IconButton from "./IconButton.vue";

const props = defineProps<{ roles: AuthorRole[]; kind: Kind }>();

const name = defineModel<string>("name", { required: true });

const emit = defineEmits<{ remove: [] }>();

const { t } = useI18n();

const nameBox = useTemplateRef("nameBox");

defineExpose({ focusName });

function focusName() {
  nameBox.value?.focus();
}

const trash = computed(() =>
  name.value === ""
    ? t("isbn.form.authors.removeUnnamed")
    : t("isbn.form.authors.remove", { name: name.value }),
);
const words = computed(() =>
  props.roles.map((role) => t(`role.${props.kind}.${role}`)).join(", "),
);
const label = computed(() =>
  [t("isbn.form.authors.roles"), words.value].filter(Boolean).join(" "),
);
</script>

<template>
  <div
    class="rounded-xl border-[1.5px] border-border bg-surface focus-within:border-accent"
  >
    <div class="flex items-center pr-0.75">
      <input
        ref="nameBox"
        v-model="name"
        type="text"
        :aria-label="t('isbn.form.authors.name')"
        class="h-12 min-w-0 flex-1 bg-transparent pl-3.5 text-field outline-none"
      />
      <IconButton :label="trash" @click="emit('remove')">
        <Trash2 :size="22" />
      </IconButton>
    </div>
    <div class="mx-3.5 border-t border-border"></div>
    <button
      type="button"
      aria-expanded="false"
      :aria-label="label"
      class="flex min-h-11 w-full items-center gap-2 pr-0.75 pl-3.5 text-left text-body outline-none"
    >
      <span class="flex-1">{{ words }}</span>
      <span class="flex size-11 shrink-0 items-center justify-center">
        <ChevronDown :size="22" class="text-muted" />
      </span>
    </button>
  </div>
</template>
