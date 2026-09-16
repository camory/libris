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
    <div class="mx-auto grid w-full max-w-120 grid-cols-3">
      <RouterLink
        to="/"
        :aria-label="t('tabs.home')"
        class="flex h-14 items-center justify-center"
        exact-active-class="text-accent"
      >
        <IconHome class="size-7" />
      </RouterLink>
      <div class="flex h-14 items-center justify-center">
        <RouterLink
          to="/isbn"
          :aria-label="t('tabs.add')"
          class="flex size-12 items-center justify-center rounded-full bg-accent text-white active:bg-accent-pressed"
        >
          <IconBarcode class="size-8" />
        </RouterLink>
      </div>
    </div>
  </nav>
</template>
