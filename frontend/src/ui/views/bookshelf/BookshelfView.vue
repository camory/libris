<script setup lang="ts">
import { CirclePlus } from "@lucide/vue";
import { computed, inject, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { bookshelvesApiKey } from "../../../application/BookshelvesApi";
import { meApiKey } from "../../../application/MeApi";
import { readersApiKey } from "../../../application/ReadersApi";
import { useAddMemberToBookshelf } from "../../../application/useAddMemberToBookshelf";
import { useFindBookshelf } from "../../../application/useFindBookshelf";
import {
  membersInOrder,
  nonMembersInOrder,
  type Member,
} from "../../../domain/Member";
import type { MembershipRole } from "../../../domain/Membership";
import type { Reader } from "../../../domain/Reader";
import IconAlert from "../../components/icons/IconAlert.vue";
import MemberRow from "../../components/MemberRow.vue";
import MemberRowSkeleton from "../../components/MemberRowSkeleton.vue";
import PageTitleSkeleton from "../../components/PageTitleSkeleton.vue";
import ReaderChoice from "../../components/ReaderChoice.vue";

const props = defineProps<{ id: string }>();

const { t, locale } = useI18n();
const reader = ref<Reader>();
const choosing = ref(false);

const meApi = inject(meApiKey)!;
const bookshelvesApi = inject(bookshelvesApiKey)!;
const readersApi = inject(readersApiKey)!;
const { state, find } = useFindBookshelf(bookshelvesApi, readersApi);
const {
  state: addState,
  add: addMember,
  reset: forgetAdded,
} = useAddMemberToBookshelf(bookshelvesApi);

const everyMember = computed<Member[]>(() =>
  state.value.status === "found"
    ? [...state.value.members, ...addState.value.added]
    : [],
);

const members = computed(() =>
  reader.value && state.value.status === "found"
    ? membersInOrder(everyMember.value, reader.value.id, locale.value)
    : null,
);

const nonMembers = computed(() =>
  state.value.status === "found"
    ? nonMembersInOrder(state.value.readers, everyMember.value, locale.value)
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

async function add(readerId: string, role: MembershipRole) {
  await addMember(props.id, readerId, role);
  if (addState.value.status === "added") choosing.value = false;
}

meApi.currentReader().then((current) => {
  reader.value = current;
});
watch([() => props.id, reader], ([id, current]) => {
  choosing.value = false;
  forgetAdded();
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
            v-if="members && nonMembers.length > 0 && !choosing"
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

      <ReaderChoice
        v-if="choosing"
        class="pt-5"
        :readers="nonMembers"
        :adding="addState.status === 'adding'"
        @add="add"
      />
    </template>
  </main>
</template>
