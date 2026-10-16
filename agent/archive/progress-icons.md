# Libris — Progress log, Icons

The entry of `agent/PROGRESS.md` of the icons, T100, done 2026-10-16,
moved here on 2026-10-16 once what a run needs of it was in
`agent/gotchas/`. Nothing is appended here.

---

## 2026-10-16 — T100 The icons are Lucide's — done
- Did: the 22 places that drew a hand-drawn icon draw `House`,
  `ScanBarcode`, `Library`, `BookOpen`, `CircleAlert`, `Search` or
  `RefreshCw` from `@lucide/vue`; `src/ui/components/icons` is gone.
- Decided: a new icon joins the file's existing `@lucide/vue` import,
  names sorted, or opens a new one first in the file, where the other
  Lucide imports sit; the tags Prettier wraps (the banner's refresh, the
  card's cover stand-in) take one attribute per line.
- Deviations from the brief: none.
- Left over: U07's wording, "the stand-in of a row's missing cover",
  proposed in the pull request body; the message and cover components
  stay in `agent/PROPOSED.md`.
