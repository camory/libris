export function languageWord(code: string, locale: string): string {
  return new Intl.DisplayNames([locale], { type: "language" }).of(code) ?? code;
}
