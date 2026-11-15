<script setup lang="ts">
import FailureMessage from "./FailureMessage.vue";

defineProps<{
  status: "notFound" | "unavailable";
  notFound: string;
  unavailable: string;
}>();
</script>

<template>
  <FailureMessage
    :sentence="status === 'unavailable' ? unavailable : notFound"
  />
</template>
