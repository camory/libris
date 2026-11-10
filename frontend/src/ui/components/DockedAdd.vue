<script setup lang="ts">
import { CircleAlert } from "@lucide/vue";
import { useI18n } from "vue-i18n";
import BusySpinner from "./BusySpinner.vue";

defineProps<{
  action: "add" | "save";
  adding: boolean;
  failed: boolean;
}>();

const emit = defineEmits<{ add: [] }>();

const words = {
  add: { idle: "isbn.add", busy: "isbn.adding", error: "isbn.addError" },
  save: { idle: "isbn.save", busy: "isbn.saving", error: "isbn.saveError" },
};

const { t } = useI18n();
</script>

<template>
  <div class="sticky bottom-0 -mx-5 [container-type:scroll-state]">
    <div
      class="flex flex-col gap-2 border-border bg-bg px-5 py-2 [@container_scroll-state(stuck:_bottom)]:border-t"
    >
      <button
        type="button"
        :disabled="adding"
        class="flex h-[50px] items-center justify-center gap-2 rounded-xl bg-accent text-button text-white active:bg-accent-pressed disabled:opacity-70"
        @click="emit('add')"
      >
        <BusySpinner v-if="adding" class="size-4" />
        {{ t(adding ? words[action].busy : words[action].idle) }}
      </button>
      <p v-if="failed" class="flex items-start gap-2 text-body text-danger">
        <CircleAlert :size="22" class="shrink-0" />
        <span>{{ t(words[action].error) }}</span>
      </p>
    </div>
  </div>
</template>
