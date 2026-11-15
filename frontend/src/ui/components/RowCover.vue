<script setup lang="ts">
import { BookOpen } from "@lucide/vue";

defineProps<{
  url: string | null;
  alt: string;
}>();
</script>

<template>
  <div
    class="flex h-18.5 w-12 shrink-0 items-center justify-center rounded-md bg-border"
  >
    <img
      v-if="url"
      :src="url"
      :alt="alt"
      class="h-full w-full rounded-md object-contain"
    />
    <BookOpen v-else :size="22" class="shrink-0 text-muted opacity-60" />
  </div>
</template>
