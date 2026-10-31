# Libris — Progress log, Summary

The entries of `agent/PROGRESS.md` of the summary, T116 to T118, done
2026-10-31, moved here on 2026-10-31 once what a run needs of them was in
`agent/gotchas/`. Nothing is appended here.

---

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

## 2026-10-31 — T118 The summary in paragraphs, folded — done
- Did: `SourceEditionCard` draws the summary one `p` per line in a block
  clamped at six lines, and *Lire la suite* (`isbn.card.readMore`) under it
  when the block overflows once mounted; a tap lifts the clamp and takes the
  button away for good. S2's two scenario cases run.
- Decided: two states in the card, `overflows` (measured in `onMounted`)
  and `unfolded` (the tap), the clamp bound to the second alone so the
  measure reads the clamped block. The 8 between paragraphs is `space-y-2`,
  margins, as the brief advised under the clamp's `-webkit-box`. The
  button's 12 of padding is pulled outside the summary's left edge with
  `-ml-3`. The S2 red was read uncommitted before step 1, as the brief
  allowed; the un-skip was committed last, as step 5.
- Deviations from the brief: steps 2 to 4 await `nextTick()` after the
  mount, since the measure lands in `onMounted` and the button on the
  render after it.
- Left over: the fold at six lines on …, the button's place, the 8 between
  paragraphs and the short summary with no button, for Tophe on the Pixel.
