<script setup lang="ts">
import { BookOpen } from "@lucide/vue";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { CoverCandidate } from "../../domain/Cover";

const props = defineProps<{
  covers: CoverCandidate[];
  title: string;
  held: boolean;
}>();

const emit = defineEmits<{ coverSource: [source: string | null] }>();

const { t } = useI18n();

const failed = ref<number[]>([]);
const offered = computed(() =>
  props.covers
    .map((cover, index) => ({ cover, index }))
    .filter(({ index }) => !failed.value.includes(index)),
);
const tapped = ref<number | null>(null);
const shown = computed(
  () =>
    offered.value.find(({ index }) => index === tapped.value) ??
    offered.value[0],
);

watch(
  () => shown.value?.cover.source ?? null,
  (source) => emit("coverSource", source),
  { immediate: true },
);

function isShown(index: number) {
  return index === shown.value?.index;
}
</script>

<template>
  <div class="flex w-24 shrink-0 flex-col">
    <div
      class="flex h-[149px] w-24 shrink-0 items-center justify-center rounded-md bg-border"
    >
      <img
        v-for="(cover, index) in covers"
        :key="cover.url"
        :src="cover.url"
        :alt="t('isbn.card.cover', { title })"
        :aria-hidden="!isShown(index)"
        class="h-full w-full rounded-md object-contain"
        :class="{ hidden: !isShown(index) }"
        @error="failed.push(index)"
      />
      <BookOpen
        v-if="!shown"
        :size="48"
        class="shrink-0 text-muted opacity-60"
      />
    </div>
    <div v-if="!held" class="flex justify-center">
      <button
        v-for="{ cover, index } in offered"
        :key="cover.url"
        type="button"
        :aria-label="t('isbn.card.coverSource', { source: cover.source })"
        :aria-pressed="isShown(index)"
        class="relative -my-3 flex h-11 w-8 items-center justify-center"
        @click="tapped = index"
      >
        <span
          class="size-2 rounded-full"
          :class="isShown(index) ? 'bg-accent' : 'border-[1.5px] border-muted'"
        ></span>
      </button>
    </div>
    <p v-if="!held && shown" class="text-center text-body text-muted">
      {{ shown.cover.source }}
    </p>
  </div>
</template>
