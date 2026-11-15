<script setup lang="ts">
import { Check, Crown, X } from "@lucide/vue";
import { onMounted, ref, useTemplateRef, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { KnownReader } from "../../domain/KnownReader";
import type { MembershipRole } from "../../domain/Membership";
import BusySpinner from "./BusySpinner.vue";
import FailureMessage from "./FailureMessage.vue";
import IconButton from "./IconButton.vue";

const props = defineProps<{
  readers: KnownReader[];
  adding: boolean;
  failed?: boolean;
}>();

const emit = defineEmits<{
  add: [readerId: string, role: MembershipRole];
  close: [];
}>();

const { t } = useI18n();
const chosenId = ref<string>();
const role = ref<MembershipRole>("VIEWER");
const failedId = ref<string>();

watch(
  () => props.failed,
  (failed) => {
    failedId.value = failed ? chosenId.value : undefined;
  },
);

const root = useTemplateRef("root");
onMounted(() => root.value?.scrollIntoView?.({ block: "nearest" }));

function choose(readerId: string) {
  chosenId.value = readerId;
  role.value = "VIEWER";
  failedId.value = undefined;
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
      <IconButton
        :disabled="adding"
        :label="t('bookshelf.members.close')"
        @click="emit('close')"
      >
        <X :size="22" class="text-muted" />
      </IconButton>
    </div>
    <div class="overflow-hidden rounded-[14px] border border-border bg-surface">
      <ul
        :aria-label="t('bookshelf.members.choose')"
        class="max-h-[313px] divide-y divide-border overflow-y-auto"
      >
        <li v-for="reader in readers" :key="reader.id">
          <div v-if="reader.id === chosenId" class="py-1.5 pr-1.5 pl-3.5">
            <div class="flex items-center gap-2">
              <span class="min-w-0 flex-1 text-body text-text">
                {{ reader.displayName }}
              </span>
              <IconButton
                :disabled="adding"
                :label="t('bookshelf.members.owner')"
                :aria-pressed="role === 'OWNER'"
                @click="toggleOwner"
              >
                <Crown
                  :size="22"
                  :fill="role === 'OWNER' ? 'currentColor' : 'none'"
                />
              </IconButton>
              <IconButton
                :disabled="adding"
                :label="
                  adding
                    ? t('bookshelf.members.adding')
                    : t('bookshelf.members.add')
                "
                @click="emit('add', reader.id, role)"
              >
                <BusySpinner v-if="adding" class="size-5.5" />
                <Check v-else :size="22" :stroke-width="3" />
              </IconButton>
            </div>
            <FailureMessage
              v-if="reader.id === failedId"
              class="pb-2"
              :sentence="t('bookshelf.members.unavailable')"
            />
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
