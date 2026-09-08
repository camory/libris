<script setup lang="ts">
import AppFooter from "./components/AppFooter.vue";

const revision = import.meta.env.VITE_APP_VERSION ?? "dev";
</script>

<template>
  <RouterView />
  <AppFooter :revision="revision" />
</template>
