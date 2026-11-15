<script setup lang="ts">
import { BookOpen } from "@lucide/vue";
import { ref } from "vue";

defineProps<{
  url: string | null;
  alt: string;
}>();

const failed = ref(false);
</script>

<template>
  <div
    class="flex h-18.5 w-12 shrink-0 items-center justify-center rounded-md bg-border"
  >
    <img
      v-if="url && !failed"
      :src="url"
      :alt="alt"
      class="h-full w-full rounded-md object-contain"
      @error="failed = true"
    />
    <BookOpen v-else :size="22" class="shrink-0 text-muted opacity-60" />
  </div>
</template>
