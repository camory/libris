<script setup lang="ts">
import { CircleAlert } from "@lucide/vue";

defineProps<{
  sentence: string;
}>();
</script>

<template>
  <p class="flex items-start gap-2 text-body text-danger">
    <CircleAlert :size="22" class="shrink-0" />
    <span>{{ sentence }}</span>
  </p>
</template>
