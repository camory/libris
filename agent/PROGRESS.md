# Libris — Progress log

Append-only. Newest entry last. One entry per loop iteration (or per human
session that changed something a future run must know).

An entry is the story of one task in about twenty-five lines, never more
than forty. It repeats nothing the pull request holds: what was built and
how it was verified are its body, and the diff is the diff. A fact a future
run must know goes in `agent/gotchas/`, not here. The entries of a
finished phase move to `agent/archive/`, one file per phase.

Format:

```
## YYYY-MM-DD — T### short title — status
- Did: what exists now that did not, in three lines at most
- Decided: one item per choice the brief left open, what and why (or "nothing")
- Deviations from the brief: what and why (or "none")
- Left over: what the task leaves to another, and where it is written (or "nothing")
```

---

## 2026-10-07 — Catalogue and covers done, with Tophe
- Did: Tophe checked the *Done* of `specs/catalogue.md` and `specs/covers.md`
  on the Pixel; both specs read done 2026-10-07, and the catalogue, web
  layer and covers sections of `agent/TASKS.md` are one line each under
  *Done*. Their entries moved to `agent/archive/progress-catalogue.md` (T042
  to T050) and `agent/archive/progress-covers.md` (T051 to T071, T068 and
  the sessions with Tophe between them).
- Decided: nothing.
- Deviations from the brief: none; no brief.
- Left over: the backlog is empty; the next feature waits for its spec,
  written with Tophe.

## 2026-10-14 — Bookshelves and the order on the screen done, with Tophe
- Did: Tophe checked the *Done* of `specs/bookshelves.md` on the Pixel on
  staging, the second account reached on a second port of the staging
  proxy. The spec is done; the Order on the screen (T086–T087) and
  Bookshelves (T072–T090) sections of `agent/TASKS.md` are one line each
  under *Done*, and the staging question left with D09's staging bullet.
- Decided: their entries moved to `agent/archive/progress-order.md` and
  `agent/archive/progress-bookshelves.md`, as the phases before them did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

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
