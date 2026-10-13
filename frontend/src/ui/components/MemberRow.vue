<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { Member } from "../../domain/Member";

defineProps<{ member: Member; isReader: boolean }>();

const { t } = useI18n();
</script>

<template>
  <i18n-t
    keypath="bookshelf.members.member"
    tag="p"
    class="p-3.5 text-body text-muted"
  >
    <template #name>
      <span class="text-text">{{
        isReader ? t("bookshelf.members.you") : member.reader.displayName
      }}</span>
    </template>
    <template #role>{{ t(`bookshelf.members.role.${member.role}`) }}</template>
  </i18n-t>
</template>
