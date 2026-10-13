<script setup lang="ts">
import { computed, inject, ref } from "vue";
import { useI18n } from "vue-i18n";
import { bookshelvesApiKey } from "../../../application/BookshelvesApi";
import { meApiKey } from "../../../application/MeApi";
import { useFindBookshelf } from "../../../application/useFindBookshelf";
import { membersInOrder } from "../../../domain/Member";
import type { Reader } from "../../../domain/Reader";
import IconAlert from "../../components/icons/IconAlert.vue";
import MemberRow from "../../components/MemberRow.vue";
import MemberRowSkeleton from "../../components/MemberRowSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";

const props = defineProps<{ id: string }>();

const { t, locale } = useI18n();
const reader = ref<Reader>();

const meApi = inject(meApiKey)!;
const bookshelvesApi = inject(bookshelvesApiKey)!;
const { state, find } = useFindBookshelf(bookshelvesApi);

const members = computed(() =>
  reader.value && state.value.status === "found"
    ? membersInOrder(state.value.members, locale.value)
    : null,
);

meApi.currentReader().then((current) => {
  reader.value = current;
});
void find(props.id);
</script>

<template>
  <main class="mx-auto w-full max-w-120 px-5 pb-5">
    <p
      v-if="state.status === 'notFound'"
      class="flex items-start gap-2 pt-5 text-body text-danger"
    >
      <IconAlert />
      <span>{{ t("bookshelf.notFound") }}</span>
    </p>
    <template v-else>
      <header class="pt-5 pb-3">
        <h1 v-if="state.status === 'found'" class="text-page-title">
          {{ state.bookshelf.name }}
        </h1>
        <PageTitleSkeleton v-else />
      </header>

      <section>
        <h2 class="flex min-h-11 items-center pb-2 text-label text-muted">
          {{ t("bookshelf.members.label") }}
        </h2>
        <div
          class="overflow-hidden rounded-[14px] border border-border bg-surface"
        >
          <ul
            v-if="members"
            :aria-label="t('bookshelf.members.label')"
            class="divide-y divide-border"
          >
            <li v-for="member in members" :key="member.reader.id">
              <MemberRow
                :member="member"
                :is-reader="member.reader.id === reader?.id"
              />
            </li>
          </ul>
          <div v-else class="divide-y divide-border">
            <MemberRowSkeleton v-for="row in 2" :key="row" />
          </div>
        </div>
      </section>
    </template>
  </main>
</template>
