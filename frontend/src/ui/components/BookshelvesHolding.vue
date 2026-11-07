<script setup lang="ts">
import { BookOpen, Check, CircleAlert, Trash2, X } from "@lucide/vue";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { bookshelvesHolding, type Copy } from "../../domain/Copy";
import BusySpinner from "./BusySpinner.vue";
import IconButton from "./IconButton.vue";

const props = defineProps<{
  copies: Copy[];
  removing?: boolean;
  removalFailed?: boolean;
}>();

const emit = defineEmits<{ remove: [copyId: string] }>();

const { t, locale } = useI18n();

const bookshelves = computed(() =>
  bookshelvesHolding(props.copies, locale.value),
);

const asking = ref<string | null>(null);

watch(
  () => props.copies,
  () => (asking.value = null),
);

function remove(bookshelfId: string) {
  const copy = props.copies.find((copy) => copy.bookshelf.id === bookshelfId);
  if (copy) emit("remove", copy.id);
}
</script>

<template>
  <div class="text-body font-semibold">
    <template v-for="{ bookshelf, count } in bookshelves" :key="bookshelf.id">
      <div
        v-if="asking === bookshelf.id"
        class="rounded-[10px] bg-bg p-2 font-normal"
      >
        <div class="flex items-start gap-2">
          <p class="min-w-0 flex-1 text-body">
            {{ t("isbn.card.removal.question", { bookshelf: bookshelf.name }) }}
          </p>
          <IconButton
            :label="t('isbn.card.removal.cancel')"
            :disabled="removing"
            class="-my-3"
            @click="asking = null"
          >
            <X :size="22" class="text-muted" />
          </IconButton>
          <IconButton
            :label="
              removing
                ? t('isbn.card.removal.removing')
                : t('isbn.card.removal.confirm')
            "
            :disabled="removing"
            class="-my-3"
            @click="remove(bookshelf.id)"
          >
            <BusySpinner v-if="removing" class="size-5.5 text-danger" />
            <Check v-else :size="22" :stroke-width="3" class="text-danger" />
          </IconButton>
        </div>
        <p
          v-if="removalFailed"
          class="flex items-start gap-2 pt-2 text-body text-danger"
        >
          <CircleAlert :size="22" class="shrink-0" />
          <span>{{ t("isbn.card.removal.failed") }}</span>
        </p>
      </div>
      <div v-else class="flex items-start gap-2">
        <BookOpen :size="22" class="my-2.75 shrink-0" />
        <div class="min-w-0 flex-1 py-3">
          <p>{{ t("isbn.card.bookshelf", { bookshelf: bookshelf.name }) }}</p>
          <p v-if="count > 1" class="font-normal text-muted">
            {{ t("isbn.card.count", { count }) }}
          </p>
        </div>
        <IconButton
          v-if="bookshelf.role === 'OWNER'"
          :label="t('isbn.card.removal.trash')"
          :disabled="removing"
          @click="asking = bookshelf.id"
        >
          <Trash2 :size="22" />
        </IconButton>
      </div>
    </template>
    <div v-if="bookshelves.length === 0" class="flex items-start gap-2">
      <BookOpen :size="22" class="my-2.75 shrink-0" />
      <p class="py-3">{{ t("isbn.card.noCopy") }}</p>
    </div>
  </div>
</template>
