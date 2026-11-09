import { nameOrder } from "./nameOrder";

export type NameStatus = "existing" | "new";

export function nameStatus(
  typed: string,
  names: string[] | null,
  locale: string,
): NameStatus | null {
  const text = typed.trim();
  if (text === "" || names === null) return null;
  const byName = nameOrder(locale);
  return names.some((name) => byName(name, text) === 0) ? "existing" : "new";
}
