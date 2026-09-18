<script setup lang="ts">
import { inject, ref } from "vue";
import { appUpdateKey } from "../application/AppUpdate";
import AppTabBar from "./components/AppTabBar.vue";
import AppUpdateBanner from "./components/AppUpdateBanner.vue";

const appUpdate = inject(appUpdateKey)!;
const state = ref<"none" | "ready" | "updating">("none");
appUpdate.onNewVersion(() => {
  state.value = "ready";
});

function update() {
  state.value = "updating";
  appUpdate.install();
}
</script>

<template>
  <div class="flex h-dvh flex-col">
    <AppUpdateBanner :state="state" @update="update" />
    <div class="min-h-0 flex-1 overflow-y-auto">
      <RouterView />
    </div>
    <AppTabBar />
  </div>
</template>
