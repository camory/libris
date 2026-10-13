<script setup lang="ts">
import { CirclePlus } from "@lucide/vue";
import { computed, inject, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { bookshelvesApiKey } from "../../../application/BookshelvesApi";
import { meApiKey } from "../../../application/MeApi";
import { readersApiKey } from "../../../application/ReadersApi";
import { useFindBookshelf } from "../../../application/useFindBookshelf";
import { membersInOrder, nonMembersInOrder } from "../../../domain/Member";
import type { Reader } from "../../../domain/Reader";
import IconAlert from "../../components/icons/IconAlert.vue";
import MemberRow from "../../components/MemberRow.vue";
import MemberRowSkeleton from "../../components/MemberRowSkeleton.vue";
import NewMemberRow from "../../components/NewMemberRow.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";

const props = defineProps<{ id: string }>();

const { t, locale } = useI18n();
const reader = ref<Reader>();
const choosing = ref(false);

const meApi = inject(meApiKey)!;
const bookshelvesApi = inject(bookshelvesApiKey)!;
const readersApi = inject(readersApiKey)!;
const { state, find } = useFindBookshelf(bookshelvesApi, readersApi);

const members = computed(() =>
  reader.value && state.value.status === "found"
    ? membersInOrder(state.value.members, locale.value)
    : null,
);

const nonMembers = computed(() =>
  state.value.status === "found"
    ? nonMembersInOrder(state.value.readers, state.value.members, locale.value)
    : [],
);

const failure = computed(() => {
  switch (state.value.status) {
    case "notFound":
      return t("bookshelf.notFound");
    case "unavailable":
      return t("bookshelf.error");
    default:
      return undefined;
  }
});

meApi.currentReader().then((current) => {
  reader.value = current;
});
watch([() => props.id, reader], ([id, current]) => {
  if (current) void find(id, current.id);
});
</script>

<template>
  <main class="mx-auto w-full max-w-120 px-5 pb-5">
    <p v-if="failure" class="flex items-start gap-2 pt-5 text-body text-danger">
      <IconAlert />
      <span>{{ failure }}</span>
    </p>
    <template v-else>
      <header class="pt-5 pb-3">
        <h1 v-if="state.status === 'found'" class="text-page-title">
          {{ state.bookshelf.name }}
        </h1>
        <PageTitleSkeleton v-else />
      </header>

      <section>
        <div class="flex min-h-11 items-center justify-between pb-2">
          <h2 class="text-label text-muted">
            {{ t("bookshelf.members.label") }}
          </h2>
          <button
            v-if="members && !choosing"
            type="button"
            :aria-label="t('bookshelf.members.new')"
            class="flex size-11 items-center justify-center rounded-[10px] text-accent"
            @click="choosing = true"
          >
            <CirclePlus />
          </button>
        </div>
        <div
          class="overflow-hidden rounded-[14px] border border-border bg-surface"
        >
          <ul
            v-if="members && !choosing"
            :aria-label="t('bookshelf.members.label')"
            class="divide-y divide-border"
          >
            <li v-for="member in members" :key="member.reader.id">
              <MemberRow
                :member="member"
                :is-reader="member.reader.id === reader?.id"
              />
            </li>
            <li v-if="choosing">
              <NewMemberRow :readers="nonMembers" :adding="false" />
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
