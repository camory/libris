<script setup lang="ts">
import { Search, X } from "@lucide/vue";
import { useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import IconButton from "./IconButton.vue";

const text = defineModel<string>({ required: true });
const emit = defineEmits<{ search: [text: string] }>();
const box = useTemplateRef("box");

let pending: number | undefined;

function pause() {
  clearTimeout(pending);
  pending = setTimeout(() => emit("search", text.value), 300);
}

function askAtOnce() {
  clearTimeout(pending);
  emit("search", text.value);
  box.value?.blur();
}

function clear() {
  clearTimeout(pending);
  text.value = "";
  emit("search", "");
  box.value?.focus();
}

const { t } = useI18n();
</script>

<template>
  <form class="relative" @submit.prevent="askAtOnce">
    <Search
      :size="22"
      class="pointer-events-none absolute top-1/2 left-3.5 -translate-y-1/2 text-muted"
    />
    <input
      ref="box"
      v-model="text"
      type="search"
      :aria-label="t('catalogue.search.label')"
      :placeholder="t('catalogue.search.placeholder')"
      class="h-[50px] w-full rounded-xl border-[1.5px] border-border bg-surface pr-13 pl-11 text-field outline-none placeholder:text-muted focus:border-accent"
      @input="pause"
    />
    <IconButton
      v-if="text !== ''"
      :label="t('catalogue.search.clear')"
      class="absolute top-1/2 right-[3px] -translate-y-1/2"
      @click="clear"
    >
      <X :size="22" class="shrink-0" />
    </IconButton>
  </form>
</template>
