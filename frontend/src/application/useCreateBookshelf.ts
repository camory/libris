import { ref, type Ref } from "vue";
import type { Membership } from "../domain/Membership";
import type { BookshelvesApi } from "./BookshelvesApi";

export type CreateState =
  | { status: "ready" }
  | { status: "creating" }
  | { status: "created"; membership: Membership };

export function useCreateBookshelf(bookshelvesApi: BookshelvesApi): {
  state: Ref<CreateState>;
  create: (name: string) => Promise<void>;
} {
  const state = ref<CreateState>({ status: "ready" });

  async function create(name: string) {
    const answer = await bookshelvesApi.create(name);
    if (answer.outcome === "created")
      state.value = {
        status: "created",
        membership: { bookshelf: answer.bookshelf, role: "OWNER" },
      };
  }

  return { state, create };
}
