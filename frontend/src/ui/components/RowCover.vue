<script setup lang="ts">
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
      :src="url ?? undefined"
      :alt="alt"
      class="h-full w-full rounded-md object-contain"
    />
  </div>
</template>
