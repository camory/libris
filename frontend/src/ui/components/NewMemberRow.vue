<script setup lang="ts">
import { Check, ChevronDown, Crown } from "@lucide/vue";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import type { KnownReader } from "../../domain/KnownReader";
import type { MembershipRole } from "../../domain/Membership";

defineProps<{
  readers: KnownReader[];
  adding: boolean;
}>();

const emit = defineEmits<{ add: [readerId: string, role: MembershipRole] }>();

const { t } = useI18n();
const readerId = ref("");
const role = ref<MembershipRole>("VIEWER");

function toggleOwner() {
  role.value = role.value === "OWNER" ? "VIEWER" : "OWNER";
}
</script>

<template>
  <div class="flex items-center gap-2 py-1.5 pr-1.5 pl-3.5">
    <div class="flex min-w-0 flex-1 items-center gap-2">
      <select
        v-model="readerId"
        :aria-label="t('bookshelf.members.reader')"
        class="field-sizing-content min-w-0 appearance-none bg-transparent text-field outline-none"
        :class="readerId === '' ? 'text-muted' : 'text-text'"
      >
        <option value="">{{ t("bookshelf.members.choose") }}</option>
        <option v-for="reader in readers" :key="reader.id" :value="reader.id">
          {{ reader.displayName }}
        </option>
      </select>
      <ChevronDown class="shrink-0 text-muted" />
    </div>
    <button
      type="button"
      :aria-label="t('bookshelf.members.owner')"
      class="flex size-11 shrink-0 items-center justify-center rounded-[10px] text-accent"
      @click="toggleOwner"
    >
      <Crown />
    </button>
    <div class="size-11 shrink-0">
      <button
        v-if="readerId !== ''"
        type="button"
        :aria-label="t('bookshelf.members.add')"
        class="flex size-11 items-center justify-center rounded-[10px] text-accent"
        @click="emit('add', readerId, role)"
      >
        <Check :stroke-width="3" />
      </button>
    </div>
  </div>
</template>
