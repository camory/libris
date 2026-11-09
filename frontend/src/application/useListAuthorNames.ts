import { ref, type Ref } from "vue";
import type { AuthorsApi } from "./AuthorsApi";

export function useListAuthorNames(authorsApi: AuthorsApi): {
  names: Ref<string[] | null>;
  list: () => Promise<void>;
} {
  const names = ref<string[] | null>(null);

  async function list() {
    try {
      names.value = await authorsApi.names();
    } catch {
      names.value = null;
    }
  }

  return { names, list };
}
