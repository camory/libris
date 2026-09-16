<script setup lang="ts">
import AppFooter from "./components/AppFooter.vue";
import AppTabBar from "./components/AppTabBar.vue";

defineProps<{ revision: string }>();
</script>

<template>
  <div class="flex h-dvh flex-col">
    <div class="min-h-0 flex-1 overflow-y-auto">
      <RouterView />
    </div>
    <AppFooter :revision="revision" />
    <AppTabBar />
  </div>
</template>
