import { ref, type Ref } from "vue";
import type { Membership } from "../domain/Membership";
import type { BookshelvesApi } from "./BookshelvesApi";

export interface CreateState {
  status: "ready" | "creating" | "created";
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
    const answer = await bookshelvesApi.create(name);
    if (answer.outcome === "created")
      state.value = {
        status: "created",
        created: [...created, { bookshelf: answer.bookshelf, role: "OWNER" }],
      };
  }

  return { state, create };
}
