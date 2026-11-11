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
