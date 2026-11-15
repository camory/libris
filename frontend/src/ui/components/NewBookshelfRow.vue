<script setup lang="ts">
import { Check } from "@lucide/vue";
import { onMounted, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import { isBookshelfName } from "../../domain/Bookshelf";
import BusySpinner from "./BusySpinner.vue";
import FailureMessage from "./FailureMessage.vue";
import IconButton from "./IconButton.vue";

defineProps<{
  creating: boolean;
  failure?: "taken" | "unavailable";
}>();

const emit = defineEmits<{ create: [name: string] }>();

const { t } = useI18n();
const name = ref("");
const field = useTemplateRef("field");

onMounted(() => field.value?.focus());
</script>

<template>
  <div class="py-2.5 pr-1.5 pl-2.5">
    <div class="flex items-center gap-2">
      <input
        ref="field"
        v-model="name"
        type="text"
        :placeholder="t('home.bookshelves.name')"
        :aria-label="t('home.bookshelves.name')"
        class="h-11 min-w-0 flex-1 rounded-xl border-[1.5px] border-border bg-surface px-3 text-field outline-none placeholder:text-muted focus:border-accent"
      />
      <div class="size-11 shrink-0">
        <IconButton
          v-if="creating || isBookshelfName(name)"
          :disabled="creating"
          :label="
            creating
              ? t('home.bookshelves.creating')
              : t('home.bookshelves.create')
          "
          @click="emit('create', name)"
        >
          <BusySpinner v-if="creating" class="size-5.5" />
          <Check v-else :size="22" :stroke-width="3" />
        </IconButton>
      </div>
    </div>
    <FailureMessage
      v-if="failure"
      class="pt-2 pb-1"
      :sentence="t(`home.bookshelves.failure.${failure}`)"
    />
  </div>
</template>
