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

## 2026-10-16 — Series list done, with Tophe
- Did: Tophe checked the *Done* of `specs/series-list.md` on the Pixel on
  staging. The spec is done; the Series list section (T092–T099) of
  `agent/TASKS.md` is one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-series-list.md`, as
  the phases before it did.
- Left over: the backlog is empty; the next feature, the series page, needs
  its spec, written with Tophe.

## 2026-10-16 — Icons done, with Tophe
- Did: Tophe checked the *Done* of T100 on the Pixel on staging. The Icons
  section (T100) of `agent/TASKS.md` is one line under *Done*.
- Decided: its entry moved to `agent/archive/progress-icons.md`, as the
  phases before it did; U07's wording, "the stand-in of a row's missing
  cover", landed with T100's pull request on Tophe's go.
- Left over: the backlog is empty; the next feature, the series page, needs
  its spec, written with Tophe.

## 2026-10-20 — Series aggregate and series page done, with Tophe
- Did: Tophe checked the *Done* of `specs/series-page.md` on the Pixel on
  staging. The spec is done; the Series aggregate (T109–T110) and Series
  page (T101–T108) sections of `agent/TASKS.md` are one line each under
  *Done*.
- Decided: their entries moved to `agent/archive/progress-series-aggregate.md`
  and `agent/archive/progress-series-page.md`, as the phases before them did.
- Left over: the backlog is empty; the next feature, the series details,
  needs its spec, written with Tophe.

## 2026-10-23 — Edition page done, with Tophe
- Did: Tophe checked the *Done* of `specs/edition-page.md` on staging. The
  spec is done; the Edition page section (T111–T115) of `agent/TASKS.md` is
  one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-edition-page.md`, as
  the phases before it did.
- Left over: the backlog is empty; the next feature needs its spec, written
  with Tophe.

## 2026-10-29 — T116 The BnF gives a summary — done
- Did: the BnF source answers the summary of its record's 330 `$a`,
  cleaned by `summaryOf` in `BnfSummary.kt`; S1's two scenario cases run
  and pass, and the provisional record's summary comes verbatim.
- Decided: the markup is dropped before its entities are decoded, so step 5
  was a guard, not a red. A line break is `</p>` directly followed by a
  `<p>` with or without attributes (`<p(?:\s[^>]*)?>`, so `<pre>` is not
  one), or `<br>` with any spaces before an optional `/`. The run of line
  feeds is collapsed after decoding, then the run of spaces, then the
  glued marks, then the trim: no clause's output feeds an earlier one.
  Step 10 was a guard too. A numeric reference outside Unicode would throw;
  left to `agent/PROPOSED.md`, no record has shown one.
- Deviations from the brief: *Mont Tombe* went green at step 7, not 13: its
  summary needs only markup, line breaks and entities, so the brief's
  "both cases green at step 13" held for *Le Larousse du Pain* alone. One
  `style:` commit after step 2 re-indents the new constant detekt refused,
  since the cycle's commit was already made.
- Left over: the stored editions' summaries (T117), the card's paragraphs
  and fold (T118); the numeric reference above, in `agent/PROPOSED.md`.

## 2026-10-29 — T116 The BnF gives a summary — reworked
- Did: a numeric reference of the 330 that names no code point
  (`&#1114112;`, `&#99999999999;`, `&#x123456789;`) stays as written instead
  of throwing out of the BnF lookup; whitespace between `</p>` and `<p>` is
  a line feed, as an adjacent pair is.
- Decided: the reviewer's verdict of 2026-10-29 found the brief's "every
  numeric reference decoded" unmet on references past U+10FFFF or past an
  `Int`; they stay as written, as an unknown named entity does, and the
  `agent/PROPOSED.md` item filed for it is gone. Its suggestion on
  `</p> <p>` was applied, since S1 calls any paragraph end followed by a
  new paragraph a line break. Its history remark asked nothing.
- Deviations from the brief: none.
- Left over: a decoded `&#0;` or lone surrogate would reach PostgreSQL,
  in `agent/PROPOSED.md`.

## 2026-10-31 — T117 Stored editions gain their summary — done
- Did: `V013__summary_for_stored_editions`, the first Kotlin migration, a
  `@Component` of `bibliography.infrastructure.persistence` that Spring Boot
  hands to Flyway; S4's three scenario cases run and pass.
- Decided: its SQL runs through a `JdbcClient` over a
  `SingleConnectionDataSource` that does not close Flyway's connection. The
  BnF is found once, at construction, with `single { it.source == BNF }`,
  as `FetchAwaitedCovers` builds its fetches. A throw of the port is logged
  `Asking the summary of <isbn> failed` at WARN with the exception. The
  slice test's `Context` answers a fresh `FluentConfiguration`, which the
  migration never reads.
- Deviations from the brief: step 2's green took the single lookup it was
  given (`editionLookups.single()`), the smallest that passed, so S4's first
  case went green at step 4, not 3: with two lookups `single()` threw.
  Steps 3 and 4 were both red.
- Left over: with the BnF down, the start waits up to
  `LIBRIS_SOURCE_TIMEOUT` per stored edition, once; for Tophe's staging
  check, said in the PR. The card's paragraphs and fold (T118).

## 2026-10-31 — T117 Stored editions gain their summary — reworked
- Did: `StoredEdition` sits between `migrate` and `summaryFor`, and the
  test's `editionStored` above `migrationOver`, each below its first user
  in the order it is reached; the `backend.md` item on the slice context
  says it holds only while `V013` is the highest version.
- Decided: the reviewer's verdict of 2026-10-31 asked for the two moves,
  which the brief's D10 already required. Its suggestion was applied: the
  gotcha is narrowed, and the trap the next migration meets is in
  `agent/PROPOSED.md`, reasoned from Flyway's defaults, not measured.
- Deviations from the brief: none.
- Left over: the next migration's slice-context trap, in
  `agent/PROPOSED.md`.
