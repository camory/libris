<script setup lang="ts">
import { Check } from "@lucide/vue";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { isBookshelfName } from "../../domain/Bookshelf";
import BusySpinner from "./BusySpinner.vue";

defineProps<{ creating: boolean }>();

const emit = defineEmits<{ create: [name: string] }>();

const { t } = useI18n();
const name = ref("");
</script>

<template>
  <div class="flex items-center gap-2 py-1.5 pr-1.5 pl-3.5">
    <input
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
</template>
