<script setup lang="ts">
import { Check } from "@lucide/vue";
import type { NameStatus } from "../../domain/NameStatus";

defineProps<{
  status: NameStatus;
  word: string;
}>();
</script>

<template>
  <span
    class="flex items-center gap-2 text-body font-semibold"
    :class="status === 'existing' ? 'text-accent' : 'text-muted'"
  >
    <Check v-if="status === 'existing'" :size="22" class="shrink-0" />
    {{ word }}
  </span>
</template>
