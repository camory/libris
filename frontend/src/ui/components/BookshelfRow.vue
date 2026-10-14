<script setup lang="ts">
import { ChevronRight, Star } from "@lucide/vue";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Membership } from "../../domain/Membership";

const props = defineProps<{ membership: Membership; isDefault: boolean }>();

const emit = defineEmits<{ choose: [] }>();

const { t } = useI18n();

const role = computed(() => {
  const role = t(`home.bookshelves.role.${props.membership.role}`);
  return props.isDefault ? t("home.bookshelves.default", { role }) : role;
});
</script>

<template>
  <div class="relative flex items-center">
    <RouterLink
      :to="`/bibliotheques/${membership.bookshelf.id}`"
      class="flex min-w-0 flex-1 flex-col gap-1 p-3.5 after:absolute after:inset-0"
    >
      <p class="text-row-title">{{ membership.bookshelf.name }}</p>
      {{ " " }}
      <p class="text-body text-muted">{{ role }}</p>
    </RouterLink>
    <span
      v-if="isDefault"
      class="flex size-11 shrink-0 items-center justify-center text-accent"
    >
      <Star fill="currentColor" />
    </span>
    <button
      v-else
      type="button"
      :aria-label="t('home.bookshelves.chooseDefault')"
      class="relative flex size-11 shrink-0 items-center justify-center rounded-[10px] text-accent"
      @click="emit('choose')"
    >
      <Star />
    </button>
    <ChevronRight class="mr-3.5 shrink-0 text-muted" />
  </div>
</template>
