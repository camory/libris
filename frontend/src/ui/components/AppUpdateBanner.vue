<script setup lang="ts">
import { useI18n } from "vue-i18n";
import IconRefresh from "./icons/IconRefresh.vue";

defineProps<{ state: "none" | "ready" | "updating" }>();
defineEmits<{ update: [] }>();

const { t } = useI18n();
</script>

<template>
  <div v-if="state !== 'none'" class="border-b border-border bg-surface">
    <div
      class="mx-auto flex w-full max-w-120 items-center gap-2 py-1.25 pr-2 pl-5"
    >
      <IconRefresh v-if="state === 'ready'" class="text-accent" />
      <span
        v-else
        aria-hidden="true"
        class="size-5.5 shrink-0 animate-spin rounded-full border-2 border-accent/30 border-t-accent"
      ></span>
      <span class="flex-1 text-body text-text">{{
        state === "ready" ? t("update.available") : t("update.installing")
      }}</span>
      <button
        v-if="state === 'ready'"
        type="button"
        class="h-11 px-3 text-button text-accent"
        @click="$emit('update')"
      >
        {{ t("update.install") }}
      </button>
    </div>
  </div>
</template>
