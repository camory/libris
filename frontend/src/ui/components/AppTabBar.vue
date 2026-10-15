<script setup lang="ts">
import { useI18n } from "vue-i18n";
import IconBarcode from "./icons/IconBarcode.vue";
import IconBooks from "./icons/IconBooks.vue";
import IconHome from "./icons/IconHome.vue";

const { t } = useI18n();
</script>

<template>
  <nav
    class="border-t border-border bg-surface pb-[env(safe-area-inset-bottom)] text-muted"
  >
    <div class="mx-auto grid w-full max-w-120 grid-cols-3">
      <RouterLink v-slot="{ href, navigate, isActive }" to="/" custom>
        <a
          :href="href"
          :aria-label="t('tabs.home')"
          :aria-current="isActive ? 'page' : undefined"
          class="flex h-12 items-center justify-center"
          :class="{ 'text-accent': isActive }"
          @click="navigate"
        >
          <IconHome class="size-8" />
        </a>
      </RouterLink>
      <div class="flex h-12 items-center justify-center">
        <RouterLink
          to="/isbn"
          :aria-label="t('tabs.add')"
          class="flex size-11 items-center justify-center rounded-full bg-accent text-white active:bg-accent-pressed"
        >
          <IconBarcode class="size-8" />
        </RouterLink>
      </div>
      <RouterLink v-slot="{ href, navigate, isActive }" to="/catalogue" custom>
        <a
          :href="href"
          :aria-label="t('tabs.catalogue')"
          :aria-current="isActive ? 'page' : undefined"
          class="flex h-12 items-center justify-center"
          :class="{ 'text-accent': isActive }"
          @click="navigate"
        >
          <IconBooks class="size-8" />
        </a>
      </RouterLink>
    </div>
  </nav>
</template>
