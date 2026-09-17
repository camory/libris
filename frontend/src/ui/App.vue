<script setup lang="ts">
import AppTabBar from "./components/AppTabBar.vue";
</script>

<template>
  <div class="flex h-dvh flex-col">
    <div class="min-h-0 flex-1 overflow-y-auto">
      <RouterView />
    </div>
    <AppTabBar />
  </div>
</template>
