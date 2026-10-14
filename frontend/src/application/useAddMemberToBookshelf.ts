import { ref, type Ref } from "vue";
import type { Member } from "../domain/Member";
import type { MembershipRole } from "../domain/Membership";
import type { BookshelvesApi } from "./BookshelvesApi";

export interface AddMemberState {
  status: "ready" | "adding" | "added" | "unavailable";
  added: Member[];
}

export function useAddMemberToBookshelf(bookshelvesApi: BookshelvesApi): {
  state: Ref<AddMemberState>;
  add: (
    bookshelfId: string,
    readerId: string,
    role: MembershipRole,
  ) => Promise<void>;
  reset: () => void;
} {
  const state = ref<AddMemberState>({ status: "ready", added: [] });

  async function add(
    bookshelfId: string,
    readerId: string,
    role: MembershipRole,
  ) {
    const { added } = state.value;
    state.value = { status: "adding", added };
    try {
      const answer = await bookshelvesApi.addMember(
        bookshelfId,
        readerId,
        role,
      );
      if (answer.outcome === "added")
        state.value = { status: "added", added: [...added, answer.member] };
      else state.value = { status: "unavailable", added };
    } catch {
      state.value = { status: "unavailable", added };
    }
  }

  function reset() {
    state.value = { status: "ready", added: [] };
  }

  return { state, add, reset };
}
