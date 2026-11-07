import { ref, type Ref } from "vue";
import type { CopiesApi } from "./CopiesApi";

export interface RemoveCopyState {
  status: "ready" | "removing" | "removed";
}

export function useRemoveCopy(
  copiesApi: CopiesApi,
  afterRemoval: () => Promise<void>,
): {
  state: Ref<RemoveCopyState>;
  remove: (copyId: string) => Promise<void>;
} {
  const state = ref<RemoveCopyState>({ status: "ready" });

  async function remove(copyId: string) {
    state.value = { status: "removing" };
    await copiesApi.remove(copyId);
    await afterRemoval();
    state.value = { status: "removed" };
  }

  return { state, remove };
}
