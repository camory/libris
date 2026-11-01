<script setup lang="ts">
import { BookOpen } from "@lucide/vue";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { bookshelvesHolding, type Copy } from "../../domain/Copy";

const props = defineProps<{ copies: Copy[] }>();

const { t, locale } = useI18n();

const bookshelves = computed(() =>
  bookshelvesHolding(props.copies, locale.value),
);
</script>

<template>
  <div class="text-body font-semibold">
    <div
      v-for="{ bookshelf, count } in bookshelves"
      :key="bookshelf.id"
      class="flex items-center gap-2"
    >
      <BookOpen :size="22" class="shrink-0" />
      <i18n-t keypath="isbn.card.copies" :plural="count" tag="p">
        <template #bookshelf>{{ bookshelf.name }}</template>
        <template #count>{{ count }}</template>
      </i18n-t>
    </div>
    <div v-if="bookshelves.length === 0" class="flex items-center gap-2">
      <BookOpen :size="22" class="shrink-0" />
      <p>{{ t("isbn.card.noCopy") }}</p>
    </div>
  </div>
</template>
