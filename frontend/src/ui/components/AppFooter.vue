<script setup lang="ts">
defineProps<{ revision: string }>();
</script>

<template>
  <footer class="p-4 text-sm">{{ revision }}</footer>
</template>
