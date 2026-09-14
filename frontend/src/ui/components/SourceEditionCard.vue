<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { SourceEdition } from "../../domain/SourceEdition";

const props = defineProps<{ edition: SourceEdition }>();

const { t } = useI18n();

const overline = computed(() => {
  const series = props.edition.series;
  if (series === null) {
    return null;
  }
  if (series.volumeNumber === null) {
    return series.name;
  }
  return t("isbn.card.series", {
    name: series.name,
    volume: series.volumeNumber,
  });
});

const authorLines = computed(() => {
  const roles = new Map<string, string[]>();
  for (const author of props.edition.authors) {
    const said = roles.get(author.name) ?? [];
    said.push(t(`role.${author.role}`));
    roles.set(author.name, said);
  }
  return [...roles].map(([name, said]) => ({ name, roles: said.join(", ") }));
});
</script>

<template>
  <article
    class="flex flex-col gap-3.5 rounded-[14px] border border-border bg-surface p-4"
  >
    <div class="flex flex-col gap-1.5">
      <p v-if="overline" class="text-overline uppercase text-accent">
        {{ overline }}
      </p>
      <h2 class="text-card-title">{{ edition.title }}</h2>
      <p v-if="edition.subtitle" class="text-lead text-muted">
        {{ edition.subtitle }}
      </p>
      <i18n-t
        v-for="line in authorLines"
        :key="line.name"
        keypath="isbn.card.author"
        tag="p"
        class="text-body"
      >
        <template #name>{{ line.name }}</template>
        <template #roles>
          <span class="text-muted">{{ line.roles }}</span>
        </template>
      </i18n-t>
    </div>
  </article>
</template>
