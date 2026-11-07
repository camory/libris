import { ref, type Ref } from "vue";
import type { CopiesApi } from "./CopiesApi";

export interface RemoveCopyState {
  status: "ready" | "removing" | "removed" | "unavailable";
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
    try {
      const answer = await copiesApi.remove(copyId);
      if (answer.outcome === "problem") {
        state.value = { status: "unavailable" };
        return;
      }
    } catch {
      state.value = { status: "unavailable" };
      return;
    }
    await afterRemoval();
    state.value = { status: "removed" };
  }

  return { state, remove };
}
