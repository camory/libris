<script setup lang="ts">
defineProps<{
  sentence: string;
}>();
</script>

<template>
  <p>
    <span>{{ sentence }}</span>
  </p>
</template>
