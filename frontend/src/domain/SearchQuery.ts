export function searchQueryOf(text: string): string | null {
  const query = text.trim();
  return Array.from(query).length < 2 ? null : query;
}
