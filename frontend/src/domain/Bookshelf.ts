export interface Bookshelf {
  id: string;
  name: string;
}

export function isBookshelfName(text: string): boolean {
  return /\S/.test(text);
}
