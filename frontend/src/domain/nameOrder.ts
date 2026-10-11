export function nameOrder(locale: string): (a: string, b: string) => number {
  return new Intl.Collator(locale, { sensitivity: "base" }).compare;
}
