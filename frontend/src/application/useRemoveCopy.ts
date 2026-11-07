import { ref, type Ref } from "vue";
import type { CopiesApi } from "./CopiesApi";

export interface RemoveCopyState {
  status: "ready" | "removed";
}

export function useRemoveCopy(copiesApi: CopiesApi): {
  state: Ref<RemoveCopyState>;
  remove: (copyId: string) => Promise<void>;
} {
  const state = ref<RemoveCopyState>({ status: "ready" });

  async function remove(copyId: string) {
    await copiesApi.remove(copyId);
    state.value = { status: "removed" };
  }

  return { state, remove };
}
