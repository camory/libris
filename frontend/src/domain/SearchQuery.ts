export function searchQueryOf(text: string): string | null {
  const query = text.trim();
  return query.length < 2 ? null : query;
}
