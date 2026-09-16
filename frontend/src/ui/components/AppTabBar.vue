<script setup lang="ts">
import { useI18n } from "vue-i18n";
import IconHome from "./icons/IconHome.vue";
import IconPlus from "./icons/IconPlus.vue";

const { t } = useI18n();
</script>

<template>
  <nav
    class="border-t border-border bg-surface pb-[max(16px,env(safe-area-inset-bottom))] text-tab text-muted"
  >
    <div class="mx-auto flex w-full max-w-120">
      <RouterLink
        to="/"
        class="flex h-14 flex-1 flex-col items-center justify-center gap-2"
        exact-active-class="font-semibold text-accent"
      >
        <IconHome />
        {{ t("tabs.home") }}
      </RouterLink>
      <RouterLink
        to="/isbn"
        class="flex h-14 flex-1 flex-col items-center justify-center gap-2"
        exact-active-class="font-semibold text-accent"
      >
        <IconPlus />
        {{ t("tabs.add") }}
      </RouterLink>
    </div>
  </nav>
</template>
