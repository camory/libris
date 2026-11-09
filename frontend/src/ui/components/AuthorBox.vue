<script setup lang="ts">
import { Check, ChevronDown, Trash2 } from "@lucide/vue";
import { computed, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import {
  roleChoicesOf,
  type AuthorRole,
  type Kind,
} from "../../domain/SourceEdition";
import IconButton from "./IconButton.vue";

const props = defineProps<{
  roles: AuthorRole[];
  kind: Kind;
  disabled?: boolean;
}>();

const name = defineModel<string>("name", { required: true });

const emit = defineEmits<{ remove: [] }>();

const { t } = useI18n();

const nameBox = useTemplateRef("nameBox");
const open = ref(false);

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
  <div class="flex flex-col gap-1">
    <div
      class="rounded-xl border-[1.5px] border-border bg-surface focus-within:border-accent"
      :class="{ 'border-accent': open }"
    >
      <div class="flex items-center pr-0.75">
        <input
          ref="nameBox"
          v-model="name"
          type="text"
          :disabled="disabled"
          :aria-label="t('isbn.form.authors.name')"
          class="h-12 min-w-0 flex-1 bg-transparent pl-3.5 text-field outline-none"
        />
        <IconButton :label="trash" :disabled="disabled" @click="emit('remove')">
          <Trash2 :size="22" />
        </IconButton>
      </div>
      <div class="mx-3.5 border-t border-border"></div>
      <button
        type="button"
        :aria-expanded="open"
        :aria-label="label"
        class="flex min-h-11 w-full items-center gap-2 pr-0.75 pl-3.5 text-left text-body outline-none"
        @click="open = !open"
      >
        <span class="flex-1">{{ words }}</span>
        <span class="flex size-11 shrink-0 items-center justify-center">
          <ChevronDown
            :size="22"
            class="text-muted"
            :class="{ 'rotate-180': open }"
          />
        </span>
      </button>
    </div>
    <div
      v-if="open"
      role="group"
      :aria-label="t('isbn.form.authors.roles')"
      class="flex flex-col divide-y divide-border rounded-xl border-[1.5px] border-border bg-surface"
    >
      <button
        v-for="choice in roleChoicesOf(roles)"
        :key="choice.role"
        type="button"
        :aria-pressed="choice.chosen"
        class="mx-3.5 flex h-11 items-center gap-2 text-left text-field"
      >
        <span class="flex-1">{{ t(`role.${kind}.${choice.role}`) }}</span>
        <Check v-if="choice.chosen" :size="22" class="shrink-0 text-accent" />
      </button>
    </div>
  </div>
</template>
