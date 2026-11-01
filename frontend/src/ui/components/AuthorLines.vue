<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import {
  authorsInOrder,
  type Kind,
  type SourceAuthor,
} from "../../domain/SourceEdition";

const props = defineProps<{ authors: SourceAuthor[]; kind: Kind }>();

const { t, locale } = useI18n();

const lines = computed(() => {
  const authors = authorsInOrder(props.authors, locale.value);
  const sets = new Set(authors.map(({ roles }) => roles.join()));
  if (sets.size === 1) {
    return [{ name: authors.map(({ name }) => name).join(", "), roles: null }];
  }
  return authors.map(({ name, roles }) => ({
    name,
    roles: roles.map((role) => t(`role.${props.kind}.${role}`)).join(", "),
  }));
});
</script>

<template>
  <template v-for="line in lines" :key="line.name">
    <i18n-t
      v-if="line.roles"
      keypath="isbn.card.author"
      tag="p"
      class="text-body"
    >
      <template #name>{{ line.name }}</template>
      <template #roles>
        <span class="text-muted">{{ line.roles }}</span>
      </template>
    </i18n-t>
    <p v-else class="text-body">{{ line.name }}</p>
  </template>
</template>
