<template>
  <div
    aria-hidden="true"
    class="flex flex-col gap-3.5 rounded-[14px] border border-border bg-surface p-4 opacity-80"
  >
    <div class="flex gap-3.5">
      <div class="h-[149px] w-24 shrink-0 rounded-md bg-border"></div>
      <div class="flex grow flex-col gap-3.5">
        <div class="h-3 w-2/5 rounded-md bg-border"></div>
        <div class="h-5 w-4/5 rounded-md bg-border"></div>
        <div class="h-4 w-3/5 rounded-md bg-border"></div>
        <div class="h-4 w-1/2 rounded-md bg-border"></div>
      </div>
    </div>
    <div class="flex items-center gap-2">
      <div class="size-5.5 shrink-0 rounded-md bg-border"></div>
      <div class="h-3.5 w-3/5 rounded-md bg-border"></div>
    </div>
    <div>
      <div
        v-for="row in 3"
        :key="row"
        class="flex justify-between gap-3.5 border-t border-border py-[7px]"
      >
        <div class="my-0.75 h-3.5 w-1/4 rounded-md bg-border"></div>
        <div class="my-0.75 h-3.5 w-1/4 rounded-md bg-border"></div>
      </div>
    </div>
  </div>
</template>
