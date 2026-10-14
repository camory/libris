<script setup lang="ts">
import { Check } from "@lucide/vue";
import { onMounted, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import { isBookshelfName } from "../../domain/Bookshelf";
import BusySpinner from "./BusySpinner.vue";
import IconAlert from "./icons/IconAlert.vue";

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
  <div class="py-1.5 pr-1.5 pl-3.5">
    <div class="flex items-center gap-2">
      <input
        ref="field"
        v-model="name"
        type="text"
        :placeholder="t('home.bookshelves.name')"
        :aria-label="t('home.bookshelves.name')"
        class="min-w-0 flex-1 bg-transparent text-field outline-none placeholder:text-muted"
      />
      <div class="size-11 shrink-0">
        <button
          v-if="creating || isBookshelfName(name)"
          type="button"
          :disabled="creating"
          :aria-label="
            creating
              ? t('home.bookshelves.creating')
              : t('home.bookshelves.create')
          "
          class="flex size-11 items-center justify-center rounded-[10px] text-accent"
          @click="emit('create', name)"
        >
          <BusySpinner v-if="creating" class="size-5.5" />
          <Check v-else :stroke-width="3" />
        </button>
      </div>
    </div>
    <p v-if="failure" class="flex items-start gap-2 pb-2 text-body text-danger">
      <IconAlert />
      <span>{{ t(`home.bookshelves.failure.${failure}`) }}</span>
    </p>
  </div>
</template>
