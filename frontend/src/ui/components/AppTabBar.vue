<script setup lang="ts">
import { useI18n } from "vue-i18n";
import IconBarcode from "./icons/IconBarcode.vue";
import IconHome from "./icons/IconHome.vue";

const { t } = useI18n();
</script>

<template>
  <nav
    class="border-t border-border bg-surface pb-[max(16px,env(safe-area-inset-bottom))] text-tab text-muted"
  >
    <div class="mx-auto grid w-full max-w-120 grid-cols-3">
      <RouterLink
        to="/"
        class="flex h-14 flex-col items-center justify-center gap-2"
        exact-active-class="font-semibold text-accent"
      >
        <IconHome class="size-6" />
        {{ t("tabs.home") }}
      </RouterLink>
      <div class="relative h-14">
        <RouterLink
          to="/isbn"
          :aria-label="t('tabs.add')"
          class="absolute top-0 left-1/2 flex size-14 -translate-x-1/2 -translate-y-5 items-center justify-center rounded-full bg-accent text-white active:bg-accent-pressed"
        >
          <IconBarcode class="size-7" />
        </RouterLink>
      </div>
    </div>
  </nav>
</template>
