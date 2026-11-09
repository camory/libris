import { ref, type Ref } from "vue";
import type { AuthorsApi } from "./AuthorsApi";

export function useListAuthorNames(authorsApi: AuthorsApi): {
  names: Ref<string[] | null>;
  list: () => Promise<void>;
} {
  const names = ref<string[] | null>(null);

  async function list() {
    names.value = await authorsApi.names();
  }

  return { names, list };
}
