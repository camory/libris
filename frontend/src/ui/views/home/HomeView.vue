<script setup lang="ts">
import { computed, inject, ref } from "vue";
import { useI18n } from "vue-i18n";
import { bookshelvesApiKey } from "../../../application/BookshelvesApi";
import { meApiKey } from "../../../application/MeApi";
import { revisionKey } from "../../../application/Revision";
import { useListReaderBookshelves } from "../../../application/useListReaderBookshelves";
import { membershipsInOrder } from "../../../domain/Membership";
import type { Reader } from "../../../domain/Reader";
import BookshelfRow from "../../components/BookshelfRow.vue";
import BookshelfRowSkeleton from "../../components/BookshelfRowSkeleton.vue";

const { t, locale } = useI18n();
const reader = ref<Reader>();

const meApi = inject(meApiKey)!;
const revision = inject(revisionKey)!;
const { state, list } = useListReaderBookshelves(inject(bookshelvesApiKey)!);

const memberships = computed(() =>
  reader.value && state.value.status === "listed"
    ? membershipsInOrder(
        state.value.memberships,
        reader.value.defaultBookshelf.id,
        locale.value,
      )
    : null,
);

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
      <h2 class="pb-2 text-label text-muted">
        {{ t("home.bookshelves.label") }}
      </h2>
      <div
        v-if="memberships"
        class="overflow-hidden rounded-[14px] border border-border bg-surface"
      >
        <ul
          :aria-label="t('home.bookshelves.label')"
          class="divide-y divide-border"
        >
          <li v-for="membership in memberships" :key="membership.bookshelf.id">
            <BookshelfRow
              :membership="membership"
              :is-default="
                membership.bookshelf.id === reader?.defaultBookshelf.id
              "
            />
          </li>
        </ul>
      </div>
      <div
        v-else
        class="divide-y divide-border overflow-hidden rounded-[14px] border border-border bg-surface"
      >
        <BookshelfRowSkeleton v-for="row in 3" :key="row" />
      </div>
    </section>

    <p class="mt-5 text-label text-muted">{{ revision }}</p>
  </main>
</template>
