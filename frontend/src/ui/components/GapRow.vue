<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Run } from "../../domain/Run";
import type { Kind } from "../../domain/SourceEdition";

const props = defineProps<{ gap: Run; kind: Kind | null }>();

const { t } = useI18n();

const tomes = computed(() => props.gap.last - props.gap.first + 1);

const name = computed(() => {
  const { first, last } = props.gap;
  return t(
    `seriesPage.gap.${props.kind}`,
    { run: t("seriesPage.run", { first, last }, last - first) },
    tomes.value,
  );
});
</script>

<template>
  <div class="flex gap-3.5 bg-bg p-3.5">
    <div
      class="h-18.5 w-12 shrink-0 rounded-md border-[1.5px] border-dashed border-muted"
    ></div>
    <div class="flex min-w-0 flex-col gap-1.5">
      <p class="text-row-title text-muted">{{ name }}</p>
      {{ " " }}
      <p class="text-body text-muted">{{ t("seriesPage.gapHint", tomes) }}</p>
    </div>
  </div>
</template>
