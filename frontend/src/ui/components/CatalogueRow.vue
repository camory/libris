<script setup lang="ts">
import type { Book } from "../../domain/Book";

defineProps<{ book: Book }>();
</script>

<template>
  <div class="flex gap-3.5 p-3.5">
    <div class="flex min-w-0 flex-col gap-1.5">
      <p class="text-row-title">{{ book.title }}</p>
    </div>
  </div>
</template>
