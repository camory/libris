<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { Membership } from "../../domain/Membership";

defineProps<{ membership: Membership; isDefault: boolean }>();

const { t } = useI18n();
</script>

<template>
  <div class="flex flex-col gap-1 p-3.5">
    <p class="text-row-title">{{ membership.bookshelf.name }}</p>
    {{ " " }}
    <p class="text-body text-muted">{{ t("home.bookshelves.role.OWNER") }}</p>
  </div>
</template>
