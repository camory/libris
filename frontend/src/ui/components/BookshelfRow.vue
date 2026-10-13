<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Membership } from "../../domain/Membership";

const props = defineProps<{ membership: Membership; isDefault: boolean }>();

const { t } = useI18n();

const role = computed(() => {
  const role = t(`home.bookshelves.role.${props.membership.role}`);
  return props.isDefault ? t("home.bookshelves.default", { role }) : role;
});
</script>

<template>
  <div class="flex items-center">
    <RouterLink
      :to="`/bibliotheques/${membership.bookshelf.id}`"
      class="flex min-w-0 flex-1 flex-col gap-1 p-3.5"
    >
      <p class="text-row-title">{{ membership.bookshelf.name }}</p>
      {{ " " }}
      <p class="text-body text-muted">{{ role }}</p>
    </RouterLink>
  </div>
</template>
