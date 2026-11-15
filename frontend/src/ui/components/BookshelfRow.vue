<script setup lang="ts">
import { Star } from "@lucide/vue";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Membership } from "../../domain/Membership";
import BusySpinner from "./BusySpinner.vue";
import FailureMessage from "./FailureMessage.vue";
import IconButton from "./IconButton.vue";

const props = defineProps<{
  membership: Membership;
  isDefault: boolean;
  busy?: boolean;
  locked?: boolean;
  failed?: boolean;
}>();

const emit = defineEmits<{ choose: [] }>();

const { t } = useI18n();

const role = computed(() => {
  const role = t(`home.bookshelves.role.${props.membership.role}`);
  return props.isDefault ? t("home.bookshelves.default", { role }) : role;
});
</script>

<template>
  <div>
    <div class="relative flex items-center">
      <RouterLink
        :to="`/bibliotheques/${membership.bookshelf.id}`"
        class="flex min-w-0 flex-1 flex-col gap-1 p-3.5 after:absolute after:inset-0"
      >
        <p class="text-row-title">{{ membership.bookshelf.name }}</p>
        {{ " " }}
        <p class="text-body text-muted">{{ role }}</p>
      </RouterLink>
      <template v-if="membership.role === 'OWNER'">
        <span
          v-if="isDefault"
          class="mr-1.5 flex size-11 shrink-0 items-center justify-center text-accent"
        >
          <Star :size="22" fill="currentColor" />
        </span>
        <IconButton
          v-else
          :disabled="busy || locked"
          :label="
            busy
              ? t('home.bookshelves.choosingDefault')
              : t('home.bookshelves.chooseDefault')
          "
          class="relative mr-1.5"
          @click="emit('choose')"
        >
          <BusySpinner v-if="busy" class="size-5.5" />
          <Star v-else :size="22" />
        </IconButton>
      </template>
    </div>
    {{ " " }}
    <FailureMessage
      v-if="failed"
      class="relative -mt-1.5 px-3.5 pb-3.5"
      :sentence="t('home.bookshelves.defaultError')"
    />
  </div>
</template>
