<script setup lang="ts">
import { useI18n } from "vue-i18n";
import IconBarcode from "./icons/IconBarcode.vue";
import IconHome from "./icons/IconHome.vue";

const { t } = useI18n();
</script>

<template>
  <nav
    class="border-t border-border bg-surface pb-[max(16px,env(safe-area-inset-bottom))] text-muted"
  >
    <div class="mx-auto flex w-full max-w-120">
      <RouterLink
        to="/"
        :aria-label="t('tabs.home')"
        class="flex h-12 flex-1 items-center justify-center"
        exact-active-class="text-accent"
      >
        <IconHome class="size-7" />
      </RouterLink>
      <RouterLink
        to="/isbn"
        :aria-label="t('tabs.add')"
        class="flex h-12 flex-1 items-center justify-center"
        exact-active-class="text-accent"
      >
        <IconBarcode class="size-7" />
      </RouterLink>
    </div>
  </nav>
</template>
