<script setup lang="ts">
import AppFooter from "./components/AppFooter.vue";

defineProps<{ revision: string }>();
</script>

<template>
  <RouterView />
  <AppFooter :revision="revision" />
</template>
