import { ref, type Ref } from "vue";
import type { Membership } from "../domain/Membership";
import type { BookshelvesApi } from "./BookshelvesApi";

export interface CreateState {
  status: "ready" | "creating" | "created" | "taken" | "unavailable";
  created: Membership[];
}

export function useCreateBookshelf(bookshelvesApi: BookshelvesApi): {
  state: Ref<CreateState>;
  create: (name: string) => Promise<void>;
} {
  const state = ref<CreateState>({ status: "ready", created: [] });

  async function create(name: string) {
    const { created } = state.value;
    state.value = { status: "creating", created };
    try {
      const answer = await bookshelvesApi.create(name);
      if (answer.outcome === "created")
        state.value = {
          status: "created",
          created: [...created, { bookshelf: answer.bookshelf, role: "OWNER" }],
        };
      else if (answer.type === "/problems/conflict")
        state.value = { status: "taken", created };
      else state.value = { status: "unavailable", created };
    } catch {
      state.value = { status: "unavailable", created };
    }
  }

  return { state, create };
}
