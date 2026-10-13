export function normalised(element: Element) {
  return element.textContent?.replace(/\s+/g, " ").trim() ?? "";
}
