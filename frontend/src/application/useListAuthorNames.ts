import { ref, type Ref } from "vue";
import type { AuthorsApi } from "./AuthorsApi";

export function useListAuthorNames(authorsApi: AuthorsApi): {
  names: Ref<string[] | null>;
  list: () => Promise<void>;
} {
  const names = ref<string[] | null>(null);
  let latest = 0;

  async function list() {
    const asked = ++latest;
    const answer = await authorsApi.names().catch(() => null);
    if (asked !== latest) return;
    names.value = answer;
  }

  return { names, list };
}
