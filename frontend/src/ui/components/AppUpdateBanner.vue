<script setup lang="ts">
import { useI18n } from "vue-i18n";
import BusySpinner from "./BusySpinner.vue";
import IconRefresh from "./icons/IconRefresh.vue";

export type UpdateState = "none" | "ready" | "updating";

defineProps<{ state: UpdateState }>();
defineEmits<{ update: [] }>();

const { t } = useI18n();
</script>

<template>
  <div v-if="state !== 'none'" class="border-b border-border bg-surface">
    <div
      class="mx-auto flex w-full max-w-120 items-center gap-2 py-1.25 pr-2 pl-5"
    >
      <IconRefresh v-if="state === 'ready'" class="text-accent" />
      <BusySpinner v-else class="size-5.5 text-accent" />
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
