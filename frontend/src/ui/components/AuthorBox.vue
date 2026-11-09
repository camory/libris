<script setup lang="ts">
import { Check, ChevronDown, CircleAlert, Trash2 } from "@lucide/vue";
import { computed, onMounted, onUnmounted, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import type { AuthorRefusal } from "../../domain/EditionDraft";
import { nameStatus } from "../../domain/NameStatus";
import {
  roleChoicesOf,
  rolesToggled,
  type AuthorRole,
  type Kind,
} from "../../domain/SourceEdition";
import { suggestionsFor } from "../../domain/Suggestion";
import IconButton from "./IconButton.vue";
import NameStatusWord from "./NameStatusWord.vue";
import SuggestionList from "./SuggestionList.vue";

const props = defineProps<{
  kind: Kind;
  disabled?: boolean;
  authorNames: string[] | null;
  refusal?: AuthorRefusal | null;
}>();

const name = defineModel<string>("name", { required: true });
const roles = defineModel<AuthorRole[]>("roles", { required: true });

const emit = defineEmits<{ remove: [] }>();

const { t, locale } = useI18n();

const box = useTemplateRef("box");
const nameBox = useTemplateRef("nameBox");
const open = ref(false);
const rolesLine = useTemplateRef("rolesLine");
const rolesList = useTemplateRef("rolesList");

const focused = ref(false);
const chosen = ref(false);
const suggestions = computed(() =>
  focused.value && !chosen.value
    ? suggestionsFor(name.value, props.authorNames ?? [], locale.value)
    : [],
);

onMounted(() => document.addEventListener("click", closeOutside));
onUnmounted(() => document.removeEventListener("click", closeOutside));

defineExpose({ focusName, focusRoles });

function focusName() {
  nameBox.value?.focus();
}

function focusRoles() {
  rolesLine.value?.focus();
  box.value?.scrollIntoView?.({ block: "center" });
}

function nameFocused() {
  focused.value = true;
  chosen.value = false;
  box.value?.scrollIntoView?.({ block: "start" });
}

function choose(tapped: string) {
  name.value = tapped;
  chosen.value = true;
}

function closeOutside(event: MouseEvent) {
  const path = event.composedPath();
  if (
    path.some((node) => node === rolesLine.value || node === rolesList.value)
  ) {
    return;
  }
  open.value = false;
}

const status = computed(() =>
  nameStatus(name.value, props.authorNames, locale.value),
);
const trash = computed(() =>
  name.value === ""
    ? t("isbn.form.authors.removeUnnamed")
    : t("isbn.form.authors.remove", { name: name.value }),
);
const outline = computed(() => {
  if (props.refusal) return "border-danger";
  return open.value
    ? "border-accent"
    : "border-border focus-within:border-accent";
});
const words = computed(() =>
  roles.value.map((role) => t(`role.${props.kind}.${role}`)).join(", "),
);
const label = computed(() =>
  [t("isbn.form.authors.roles"), words.value].filter(Boolean).join(" "),
);
</script>

<template>
  <div class="flex flex-col gap-1">
    <div
      ref="box"
      class="scroll-mt-28 rounded-xl border-[1.5px] bg-surface"
      :class="outline"
    >
      <div class="flex items-center pr-0.75">
        <input
          ref="nameBox"
          v-model="name"
          type="text"
          :disabled="disabled"
          :aria-label="t('isbn.form.authors.name')"
          @focus="nameFocused"
          @blur="focused = false"
          @input="chosen = false"
          class="h-12 min-w-0 flex-1 bg-transparent pl-3.5 text-field outline-none"
        />
        <NameStatusWord
          v-if="status"
          :status="status"
          :word="t(`isbn.form.authors.nameStatus.${status}`)"
          class="mr-2 shrink-0"
        />
        <IconButton :label="trash" :disabled="disabled" @click="emit('remove')">
          <Trash2 :size="22" />
        </IconButton>
      </div>
      <div class="mx-3.5 border-t border-border"></div>
      <button
        ref="rolesLine"
        type="button"
        :disabled="disabled"
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
    <p v-if="refusal" class="mt-1 flex items-start gap-2 text-body text-danger">
      <CircleAlert :size="22" class="shrink-0" />
      <span>{{ t(`isbn.form.authors.refusal.${refusal}`) }}</span>
    </p>
    <SuggestionList
      v-if="suggestions.length"
      :suggestions="suggestions"
      @choose="choose"
    />
    <div
      v-if="open"
      ref="rolesList"
      role="group"
      :aria-label="t('isbn.form.authors.roles')"
      class="flex flex-col divide-y divide-border rounded-xl border-[1.5px] border-border bg-surface"
    >
      <button
        v-for="choice in roleChoicesOf(roles)"
        :key="choice.role"
        type="button"
        :aria-pressed="choice.chosen"
        @click="roles = rolesToggled(roles, choice.role)"
        class="mx-3.5 flex h-11 items-center gap-2 text-left text-field"
      >
        <span class="flex-1">{{ t(`role.${kind}.${choice.role}`) }}</span>
        <Check v-if="choice.chosen" :size="22" class="shrink-0 text-accent" />
      </button>
    </div>
  </div>
</template>
