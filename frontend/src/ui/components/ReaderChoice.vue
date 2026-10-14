<script setup lang="ts">
import { Check, Crown, X } from "@lucide/vue";
import { onMounted, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import type { KnownReader } from "../../domain/KnownReader";
import type { MembershipRole } from "../../domain/Membership";
import BusySpinner from "./BusySpinner.vue";

defineProps<{
  readers: KnownReader[];
  adding: boolean;
}>();

const emit = defineEmits<{
  add: [readerId: string, role: MembershipRole];
  close: [];
}>();

const { t } = useI18n();
const chosenId = ref<string>();
const role = ref<MembershipRole>("VIEWER");

const root = useTemplateRef("root");
onMounted(() => root.value?.scrollIntoView?.({ block: "nearest" }));

function choose(readerId: string) {
  chosenId.value = readerId;
  role.value = "VIEWER";
}

function toggleOwner() {
  role.value = role.value === "OWNER" ? "VIEWER" : "OWNER";
}
</script>

<template>
  <section ref="root">
    <div class="flex min-h-11 items-center justify-between pb-2">
      <h2 class="text-label text-muted">
        {{ t("bookshelf.members.choose") }}
      </h2>
      <button
        type="button"
        :disabled="adding"
        :aria-label="t('bookshelf.members.close')"
        class="flex size-11 items-center justify-center rounded-[10px] text-muted"
        @click="emit('close')"
      >
        <X />
      </button>
    </div>
    <div class="overflow-hidden rounded-[14px] border border-border bg-surface">
      <ul
        :aria-label="t('bookshelf.members.choose')"
        class="max-h-[313px] divide-y divide-border overflow-y-auto"
      >
        <li v-for="reader in readers" :key="reader.id">
          <div
            v-if="reader.id === chosenId"
            class="flex items-center gap-2 py-1.5 pr-1.5 pl-3.5"
          >
            <span class="min-w-0 flex-1 text-body text-text">
              {{ reader.displayName }}
            </span>
            <button
              type="button"
              :disabled="adding"
              :aria-label="t('bookshelf.members.owner')"
              :aria-pressed="role === 'OWNER'"
              class="flex size-11 shrink-0 items-center justify-center rounded-[10px] text-accent"
              @click="toggleOwner"
            >
              <Crown :fill="role === 'OWNER' ? 'currentColor' : 'none'" />
            </button>
            <button
              type="button"
              :disabled="adding"
              :aria-label="
                adding
                  ? t('bookshelf.members.adding')
                  : t('bookshelf.members.add')
              "
              class="flex size-11 shrink-0 items-center justify-center rounded-[10px] text-accent"
              @click="emit('add', reader.id, role)"
            >
              <BusySpinner v-if="adding" class="size-5.5" />
              <Check v-else :stroke-width="3" />
            </button>
          </div>
          <button
            v-else
            type="button"
            :disabled="adding"
            class="flex min-h-14 w-full items-center px-3.5 text-left text-body text-text"
            @click="choose(reader.id)"
          >
            {{ reader.displayName }}
          </button>
        </li>
      </ul>
    </div>
  </section>
</template>
