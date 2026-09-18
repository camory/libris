<script setup lang="ts">
import { useI18n } from "vue-i18n";
import IconRefresh from "./icons/IconRefresh.vue";

defineProps<{ state: "none" | "ready" | "updating" }>();
defineEmits<{ update: [] }>();

const { t } = useI18n();
</script>

<template>
  <div class="border-b border-border bg-surface">
    <div
      class="mx-auto flex w-full max-w-120 items-center gap-2 py-[5px] pr-2 pl-5"
    >
      <IconRefresh class="text-accent" />
      <span class="flex-1 text-body text-text">{{ t("update.available") }}</span>
      <button
        type="button"
        class="h-11 px-3 text-button text-accent"
        @click="$emit('update')"
      >
        {{ t("update.install") }}
      </button>
    </div>
  </div>
</template>
