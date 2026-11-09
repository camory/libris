<script setup lang="ts">
import { nameOf, type Suggestion } from "../../domain/Suggestion";

defineProps<{ suggestions: Suggestion[] }>();

const emit = defineEmits<{ choose: [name: string] }>();
</script>

<template>
  <div
    class="flex flex-col divide-y divide-border rounded-xl border-[1.5px] border-border bg-surface"
  >
    <button
      v-for="suggestion in suggestions"
      :key="nameOf(suggestion)"
      type="button"
      @mousedown.prevent
      @click="emit('choose', nameOf(suggestion))"
      class="mx-3.5 h-11 text-left text-field"
    >
      <span>{{ suggestion.before }}</span>
      <span class="font-semibold">{{ suggestion.matched }}</span>
      <span>{{ suggestion.after }}</span>
    </button>
  </div>
</template>
