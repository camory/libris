<script setup lang="ts">
import { CircleAlert, CirclePlus } from "@lucide/vue";
import { computed, inject, ref } from "vue";
import { useI18n } from "vue-i18n";
import { bookshelvesApiKey } from "../../../application/BookshelvesApi";
import { meApiKey } from "../../../application/MeApi";
import { revisionKey } from "../../../application/Revision";
import {
  type ChooseDefaultState,
  useChooseDefaultBookshelf,
} from "../../../application/useChooseDefaultBookshelf";
import { useCreateBookshelf } from "../../../application/useCreateBookshelf";
import { useListReaderBookshelves } from "../../../application/useListReaderBookshelves";
import { membershipsInOrder } from "../../../domain/Membership";
import type { Reader } from "../../../domain/Reader";
import BookshelfRow from "../../components/BookshelfRow.vue";
import BookshelfRowSkeleton from "../../components/BookshelfRowSkeleton.vue";
import NewBookshelfRow from "../../components/NewBookshelfRow.vue";

const { t, locale } = useI18n();
const reader = ref<Reader>();
const naming = ref(false);

const meApi = inject(meApiKey)!;
const revision = inject(revisionKey)!;
const bookshelvesApi = inject(bookshelvesApiKey)!;
const { state, list } = useListReaderBookshelves(bookshelvesApi);
const { state: createState, create: createBookshelf } =
  useCreateBookshelf(bookshelvesApi);
const { state: chooseState, choose } =
  useChooseDefaultBookshelf(bookshelvesApi);

const defaultBookshelfId = computed(
  () =>
    chooseState.value.defaultBookshelfId ?? reader.value?.defaultBookshelf.id,
);

const memberships = computed(() =>
  defaultBookshelfId.value && state.value.status === "listed"
    ? membershipsInOrder(
        [...state.value.memberships, ...createState.value.created],
        defaultBookshelfId.value,
        locale.value,
      )
    : null,
);

const createFailure = computed(() =>
  createState.value.status === "taken" ||
  createState.value.status === "unavailable"
    ? createState.value.status
    : undefined,
);

function isChoice(bookshelfId: string, status: ChooseDefaultState["status"]) {
  return (
    chooseState.value.status === status &&
    chooseState.value.bookshelfId === bookshelfId
  );
}

async function create(name: string) {
  await createBookshelf(name);
  if (createState.value.status === "created") naming.value = false;
}

meApi.currentReader().then((current) => {
  reader.value = current;
});
void list();
</script>

<template>
  <main class="mx-auto w-full max-w-120 px-5 pb-5">
    <header class="pt-5 pb-3">
      <h1 class="text-page-title">{{ t("home.title") }}</h1>
      <p v-if="reader" class="text-body text-muted">
        {{ t("home.greeting", { name: reader.displayName }) }}
      </p>
    </header>

    <section>
      <div class="flex min-h-11 items-center justify-between pb-2">
        <h2 class="text-label text-muted">
          {{ t("home.bookshelves.label") }}
        </h2>
        <button
          v-if="memberships && !naming"
          type="button"
          :aria-label="t('home.bookshelves.new')"
          class="flex size-11 items-center justify-center rounded-[10px] text-accent"
          @click="naming = true"
        >
          <CirclePlus />
        </button>
      </div>
      <p
        v-if="state.status === 'unavailable'"
        class="flex items-start gap-2 text-body text-danger"
      >
        <CircleAlert :size="22" class="shrink-0" />
        <span>{{ t("home.bookshelves.error") }}</span>
      </p>
      <div
        v-else
        class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
      >
        <ul
          v-if="memberships"
          :aria-label="t('home.bookshelves.label')"
          class="divide-y divide-border"
        >
          <li v-for="membership in memberships" :key="membership.bookshelf.id">
            <BookshelfRow
              :membership="membership"
              :is-default="membership.bookshelf.id === defaultBookshelfId"
              :busy="isChoice(membership.bookshelf.id, 'choosing')"
              :locked="chooseState.status === 'choosing'"
              :failed="isChoice(membership.bookshelf.id, 'unavailable')"
              @choose="choose(membership.bookshelf.id)"
            />
          </li>
          <li v-if="naming">
            <NewBookshelfRow
              :creating="createState.status === 'creating'"
              :failure="createFailure"
              @create="create"
            />
          </li>
        </ul>
        <template v-else>
          <BookshelfRowSkeleton v-for="row in 3" :key="row" />
        </template>
      </div>
    </section>

    <p class="mt-5 text-label text-muted">{{ revision }}</p>
  </main>
</template>
