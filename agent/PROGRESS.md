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

## 2026-10-31 — Summary done, with Tophe
- Did: Tophe checked the *Done* of `specs/summary.md` on staging. The
  spec is done; the Summary section (T116–T118) of `agent/TASKS.md` is one
  line under *Done*.
- Decided: its entries moved to `agent/archive/progress-summary.md`, as
  the phases before it did.
- Left over: the backlog is empty; the next feature, the edition form,
  needs its spec, written with Tophe.

## 2026-11-04 — Edition form done, with Tophe
- Did: Tophe checked the *Done* of `specs/edition-form.md` on staging. The
  spec is done; the Edition form section (T119–T129) of `agent/TASKS.md`
  is one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-edition-form.md`,
  as the phases before it did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

## 2026-11-07 — Remove a copy done, with Tophe
- Did: Tophe checked the *Done* of `specs/remove-copy.md` on staging. The
  spec is done; the Remove a copy section (T130–T137) of `agent/TASKS.md`
  is one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-remove-copy.md`,
  as the phases before it did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

## 2026-11-08 — Scan beep done, with Tophe
- Did: Tophe checked the *Done* of `specs/scan-beep.md` on staging. The
  spec is done; the Scan beep section (T138–T141) of `agent/TASKS.md` is
  one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-scan-beep.md`,
  as the phases before it did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

## 2026-11-09 — Authors edit done, with Tophe
- Did: Tophe checked the *Done* of `specs/authors-edit.md` on staging. The
  spec is done; the Authors edit section (T142–T152) of `agent/TASKS.md`
  is one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-authors-edit.md`,
  as the phases before it did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

## 2026-11-10 — Held edition edit done, with Tophe
- Did: Tophe checked the *Done* of `specs/held-edition-edit.md` on
  staging. The spec is done; the Held edition edit section (T153–T163) of
  `agent/TASKS.md` is one line under *Done*.
- Decided: its entries moved to
  `agent/archive/progress-held-edition-edit.md`, as the phases before it
  did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

## 2026-11-11 — T164 The search on v0.19.0, a whole ISBN — done
- Did: the backend pins `v0.19.0`; `GET /api/v1/books` reads `q` through
  `CatalogueQuery.of` and answers `near`, false; a whole ISBN narrows the
  catalogue to the reader's edition of it, any other query to none.
- Decided: `JdbcCatalogueEditions` shares the catalogue's statement with the
  search through three `const` parts and one `pageOf` (gotcha in
  `backend.md`); the ranking CTE carries `edition.isbn13`.
- Decided: the fake records a search as a `SearchAsked`, beside the
  catalogue's `asked` triples, so step 13 also proves no catalogue page was
  asked.
- Decided: step 13 went red through a behaviour, not only a compile error:
  the use case took the query unused first, which the controller passed as
  `null`, then the call to the overload greened it.
- Deviations from the brief: step 3 gave `near = false` to all three
  `CataloguePage`s of `BrowseCatalogueTest`, not the first alone, since each
  must compile. The run handed off after step 12 and was finished by a
  second run.
- Left over: words (T165), the saved edition's search columns (T166), the
  tome (T167), near matches (T168), the frontend's pin (T169).

## 2026-11-11 — T164 The search on v0.19.0, a whole ISBN — reworked
- Did: `JdbcCatalogueEditionsTest`'s five top-level fixtures (`PAGE_SIZE`,
  `LESSER_TWIN`, `GREATER_TWIN`, `LA_SERPE_D_OR`, `ASTERIX_ET_LES_GOTHS`)
  moved below the class and its helpers, in the order the class reaches
  them; no test changed.
- Decided: the reviewer's verdict of 2026-11-11 amended nothing; its
  blocking finding was the stepdown rule the brief already holds. Its
  suggestion, a refactor of green code in a commit of its own rather than
  inside step 10's green, concerns pushed history, which is not rewritten:
  nothing to apply on the branch.
- Deviations from the brief: none.
- Left over: nothing new; T165 to T169 as before.

## 2026-11-11 — T165 Words find an added edition — done
- Did: `V016` brings `unaccent`, the configuration
  `words_ignoring_case_and_accents`, `edition.search_text` (filled, then
  `NOT NULL`), the generated `search_vector` and its GIN index; the insert
  writes the text; a query that reads as no ISBN finds the reader's editions
  by its words, each a prefix, all required. S5 (an edition added) and S4 pass.
- Decided: `searchOf` picks `FIND_PAGE_OF_ISBN` or `FIND_PAGE_OF_WORDS`;
  `write` takes a `StatementSpec`, so `insert` adds `authorNames` and
  `JdbcEditionRepository` stays at ten functions; the slice test builds
  subtitle and authors through `stored(editionTitled(…).copy(…))`.
- Decided: the query's words are the lexemes of `TO_TSVECTOR` over the
  query, each quoted and suffixed `:*`, joined by `&`, so no sign of the
  `tsquery` syntax reaches `TO_TSQUERY`.
- Deviations from the brief: the extension and `unaccent` in the
  configuration came with step 3, not step 7, since a configuration on no
  extension survives Flyway's clean and breaks the next migration (gotcha);
  steps 7 and 11 are thus guards. Step 3 read the words with
  `PLAINTO_TSQUERY`, which step 8's prefixes replaced.
- Deviations from the brief: `V013__summary_for_stored_editionsTest`, a
  test file the brief did not list, gives `search_text` in its raw insert,
  which the `NOT NULL` column refused (four cases red at the gate).
- Deviations from the brief: the run handed off after step 15, its gate red
  on that test, and was finished by a second run.
- Left over: `update` writing `search_text` (T166), the tome (T167), near
  matches (T168), the frontend's pin (T169).

## 2026-11-11 — T165 Words find an added edition — reworked
- Did: `V016`'s configuration also puts `numword`, `numhword` and
  `hword_numpart` through `unaccent, simple`, so a word joining letters and
  digits is found ignoring accents; two slice cases prove it.
- Decided: the reviewer's verdict of 2026-11-11 amended nothing; its
  blocking finding was the task line's "ignoring accents" and D03, which the
  brief's *Files* line had narrowed to three token types. Its suggestion, a
  refactor of 0dd2c2cc in a commit of its own, concerns pushed history,
  which is not rewritten: nothing to apply on the branch.
- Decided: two cases, not one. `pokemon` finding *Pokémon2* proves
  `numword`; `rouge-pokemon2` finding *Rouge-Pokémon2* reds unless both
  `numhword` and `hword_numpart` are mapped (each alone run, red); with the
  digits in the first part, `numhword` alone was enough (gotcha).
- Deviations from the brief: the configuration names six token types where
  the brief's *Files* named three, as the review asks.
- Fixed up after the second review, on Tophe's call: the configuration puts
  all nineteen types `simple` maps through `unaccent, simple`, since
  `file` (`Café2.1`) and `email` kept their accents; `cafe` finding
  *Café2.1* proves it.
- Left over: nothing new; T166 to T169 as before.

## 2026-11-11 — T166 A saved edition found by its new words — done
- Did: `JdbcEditionRepository.update` writes `edition.search_text` from the
  same four fields as `insert`, so a saved edition is found by its words as
  they now stand and never by those it no longer holds; S5's second case runs.
- Decided: the two statements share the expression through the `const` part
  `SEARCH_TEXT`, and `write` sets `authorNames` itself, so it takes the
  statement's text again instead of a `StatementSpec`; the class stays at
  ten functions.
- Decided: each field joined the update's text in the cycle of its own case
  (title, subtitle, series, authors), the refactor in a commit after them.
- Deviations from the brief: none.
- Left over: an edition saved on a deployment that ran T165 without T166
  (staging alone) keeps its earlier text until saved again, no re-fill;
  the tome (T167), near matches (T168), the frontend (T169 on).

## 2026-11-11 — T167 A tome found by its number — done
- Did: the words' clause of `JdbcCatalogueEditions` asks each word of the
  query in turn: found when it begins a word of `search_vector` or, a word
  of digits alone, when it is the edition's volume number; S1 runs whole.
- Decided: each word asked through `BOOL_AND` over the query's lexemes, so
  a query of no word gives no row, `NULL`, and finds nothing without a
  clause of its own.
- Decided: the word compared as `numeric`, inside a `CASE` on
  `~ '^[0-9]+$'`: a number, so `02` is tome 2, that no length overflows,
  and a cast PostgreSQL never tries on a lexeme of letters.
- Decided: the comparison with the volume number ends `IS TRUE`: a
  lexeme of letters (its `CASE` gives `NULL`) or an edition without a
  tome answers `NULL` there, which `BOOL_AND` skips; without it *serpe
  loup* found both editions (run as an extra mutation).
- Decided: step 4 is a guard, step 2's green already comparing numbers.
- Deviations from the brief: none.
- Left over: the GIN index no longer serves the words' clause, asked row
  by row, as the brief accepted; near matches (T168), the frontend (T169
  on).

## 2026-11-12 — The tome a token of the search's words (side PR, no task) — done
- Did: `V017` adds the tome to `edition.search_vector` as one exact
  lexeme, `#` then the number; the words' clause of `JdbcCatalogueEditions`
  is one `tsquery` again, a word of digits alone asked as a prefix or as
  that token. Every answer kept: `JdbcCatalogueEditionsTest` and S1
  unchanged and green, the token mutation-checked on both sides (the
  migration and the clause, two tome cases red each).
- Decided (Tophe, #404): the token before T168, as a side PR, since a
  change keeping every answer has no red to start from.
- Measured on 10 000 synthetic editions: the same rows as T167's clause
  for ten queries; 0.3–2.7 ms against 16–59 ms, 7–9 ms for a word found
  in 3 006 editions (the sort).
- Left over: T167's left-over no longer holds, the GIN index serves the
  words' clause; near matches (T168), the frontend (T169 on).

## 2026-11-12 — T168 Near matches when the words find nothing — done
- Did: when the words find none of the reader's editions, the words'
  statement answers those whose unaccented `search_text` is near the
  unaccented query by pg_trgm's word similarity, in the catalogue's order
  and pages, the page said `near`; `V018` brings `pg_trgm`,
  `without_accents` and `edition_search_trigrams`; S3 runs, both cases.
- Decided: near is decided over the reader's whole catalogue, never the
  page after `after`. Review fix-up, Tophe's call: the statement first
  joined a CTE of the found ids back to `ranked`, 11–22 ms on 10 000
  editions where `main` took 4–6 for the same rows; it is now a `UNION
  ALL` of the found branch and the near one, gated by `NOT EXISTS` over
  the reader's whole catalogue and ordered once, 7–10 ms; `FOUND_BY_WORDS`
  serves both, `IN_CATALOGUE_ORDER` names its columns unqualified.
- Decided: `HELD_BY_READER` split out of `HELD_BY_READER_AFTER`, which is
  it plus the keyset; `RANKED` carries `search_text`.
- Decided: `pageOf` reads each row's `near` through a private `PageRow`,
  the page near when one of its rows is (all or none, by the statement).
- Decided: steps 4 to 9 green on arrival, guards, each mutation run and
  reverted (listed in the PR body); step 1's *no edition near either*
  green on arrival, as the brief measured.
- Decided: the run handed off after steps 1 to 9 and the tidy; the
  second run ran the gate, the `EXPLAIN`, the documents and the PR.
- Deviations from the brief: `FIND_PAGE` and `FIND_PAGE_OF_ISBN` select
  `FALSE AS near`, since `pageOf`, shared by the three statements, reads
  the column (the brief said they stay); one commit subject is 76
  characters, pushed before the handoff, left as it is.
- Left over: no threshold setting (out of scope); the frontend's search
  (T169 on), whose pin moves once T168 is deployed.

## 2026-11-12 — T169 The search box on v0.19.0 — handoff
- Done: step 1 (pin `v0.19.0`, `npx vitest run src/infra` 100 green); S1
  un-skipped and committed red (*Unable to find … searchbox "Rechercher dans
  le catalogue"*), it goes green last at step 19; step 2 green.
- Start with: step 3 (*fails when the API refuses the search*, on the mock).
- Decided: `search` is a method of `FetchCatalogueApi`, which now declares
  `implements CatalogueApi, SearchApi`; `after` is ignored (`void after`)
  and the page answered empty until steps 4 to 6 read them.
- No guard reached yet. Gate red by design: S1 un-skipped awaits steps 3–19.
