<script setup lang="ts">
import { BookOpen, Trash2 } from "@lucide/vue";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { bookshelvesHolding, type Copy } from "../../domain/Copy";
import IconButton from "./IconButton.vue";

const props = defineProps<{ copies: Copy[] }>();

const { t, locale } = useI18n();

const bookshelves = computed(() =>
  bookshelvesHolding(props.copies, locale.value),
);

const asking = ref(false);
</script>

<template>
  <div class="text-body font-semibold">
    <template v-for="{ bookshelf, count } in bookshelves" :key="bookshelf.id">
      <div v-if="asking" class="rounded-[10px] bg-bg p-2">
        <p class="text-body font-normal">
          {{ t("isbn.card.removal.question", { bookshelf: bookshelf.name }) }}
        </p>
      </div>
      <div v-else class="flex items-center gap-2">
        <BookOpen :size="22" class="shrink-0" />
        <i18n-t
          keypath="isbn.card.copies"
          :plural="count"
          tag="p"
          class="min-w-0 flex-1"
        >
          <template #bookshelf>{{ bookshelf.name }}</template>
          <template #count>{{ count }}</template>
        </i18n-t>
        <IconButton
          v-if="bookshelf.role === 'OWNER'"
          :label="t('isbn.card.removal.trash')"
          @click="asking = true"
        >
          <Trash2 :size="22" />
        </IconButton>
      </div>
    </template>
    <div v-if="bookshelves.length === 0" class="flex items-center gap-2">
      <BookOpen :size="22" class="shrink-0" />
      <p>{{ t("isbn.card.noCopy") }}</p>
    </div>
  </div>
</template>
