<script setup lang="ts">
defineProps<{ label: string }>();
</script>

<template>
  <button
    type="button"
    :aria-label="label"
    class="flex size-11 shrink-0 items-center justify-center rounded-[10px] text-accent"
  >
    <slot />
  </button>
</template>
