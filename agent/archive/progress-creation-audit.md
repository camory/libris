# Libris — Progress log, Creation audit

The entry of `agent/PROGRESS.md` of the creation audit, T091, done
2026-10-14, moved here on 2026-10-14 once what a run needs of it was in
`agent/gotchas/`. Nothing is appended here.

---

## 2026-10-14 — T091 a bookshelf and a copy record their creation — pr_opened
- Did: `Bookshelf` carries `createdBy` and `createdAt`, `Copy` `addedBy`
  and `addedAt`, set by `CreateBookshelf`, `WelcomeReader` and
  `AddBookToBookshelf` from the reader acting and the `Clock`; `V011` and
  `V012` add the columns, fill the rows already there, then make them
  `NOT NULL`, the reader a reference. Nothing in the API changes.
- Decided, with Tophe: the creation is an attribute of the aggregate, not
  a column the database fills (D11 reworded in this PR, "no audit
  columns" gone); a row already there gets the bookshelf's owner of the
  smallest id, the only owner unless the bookshelf was shared, and the
  migration's instant; `Bookshelf.created(...)` holds the rule that the
  creator is the one owner; `AddBookToBookshelf` reaches seven
  constructor parameters and carries `@Suppress("LongParameterList")`
  for now.
- Deviations from the brief: none; no brief, done by Claude with Tophe.
- Left over: the suppress goes with the `HoldEdition` item of
  `agent/PROPOSED.md`; no screen shows the creation yet.
