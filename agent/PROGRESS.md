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

## 2026-11-08 — T142 The authors' names on v0.17.0 — done
- Did: the backend pins `v0.17.0` and serves `GET /api/v1/author-names`
  through `ListAuthorNames` over the new query port `AuthorNames`, read by
  `JdbcAuthorNames` from `author` alone; S6 of the authors edit un-skipped.
- Decided: `JdbcAuthorNames` maps its one column with
  `query { rs, _ -> rs.getString("name") }`, since `query(String::class.java)`
  answers a `List<String?>` the port's `List<String>` refuses.
- Deviations from the brief: `@JdbcSliceTest` caps its Hikari pool at three
  connections; `JdbcAuthorNamesTest`'s context, the suite's eleventh with a
  pool of ten, ran PostgreSQL out of its 100 connections and failed the gate
  (`too many clients already`, 74 cases).
- Left over: T143 (an author's name matched as PRD §3 says) and T144 (the
  frontend onto `v0.17.0`), both in the backlog.

## 2026-11-08 — T142 The authors' names on v0.17.0 — reworked
- Did: `AuthorNamesAnswering.findAll`'s `=` body starts on the next line;
  the `JdbcClient` `query(…)` item of `agent/gotchas/backend.md` records
  that `query(String::class.java).list()` answers a `List<String?>`.
- Decided: the reviewer's verdict of 2026-11-08 amended nothing; its
  blocking finding was D10's layout, which the brief's model fake
  (`CatalogueEditionsAnswering`, line 20, outside this diff) breaks too and
  stays as it is.
- Deviations from the brief: none beyond the first run's.
- Left over: nothing new; the note on the brief's model is the reviewer's,
  for the planner.

## 2026-11-08 — T142 The authors' names on v0.17.0 — fix-up
- Did (Tophe's call, after review): every JDBC slice test shares one
  Spring context. `@JdbcSliceTest` imports the eight `Jdbc…` adapters, the
  slice tests carry no `@Import` of their own, and the pool cap of three is
  gone. The two items of `agent/gotchas/backend.md` on contexts and on
  `@JdbcSliceTest` say so, and three items naming a test's `@Import` now
  name its constructor.
- Decided: the cap treated the symptom. Each distinct `@Import` set was a
  context of its own, cached until the JVM ends, each holding a pool of ten
  connections. The suite now starts six contexts, four of them with a pool,
  40 connections at peak, measured on `pg_stat_activity`.
- Verified: `./gradlew check` on the host, 512 tests, 0 failures, 1 skipped
  (S5).
- Left over: nothing.

## 2026-11-08 — T143 An author's name matched as PRD §3 says — done
- Did: `V015` makes `author_name_unique` ignore case, accents and the
  spaces at a name's ends, as series are; `INSERT_AUTHOR` names it as its
  conflict target and `Contributions.of` folds names the same way; S5 of
  the authors edit un-skipped and green.
- Decided: the folding trims the space character alone (`trim(' ')`),
  since `BTRIM` leaves a tab.
- Deviations from the brief: none. Step 2's first green tried the bare
  column target `((name COLLATE …))`, which infers no index; the target
  takes the collation after the expression, as step 3's final form does
  anyway.
- Left over: the two private foldings (`BookshelfName.kt`,
  `Contributions.kt`) in `agent/PROPOSED.md`; T144 moves the frontend
  onto `v0.17.0`.

## 2026-11-08 — T144 The authors on v0.17.0, open as rows — done
- Did: the frontend pins `v0.17.0`; `FetchAuthorsApi` reads the authors'
  names and `authorsApi` joins `LibrisPorts`; the edition form opens each
  author as an `AuthorBox`, its name then its roles line. S1 un-skipped.
- Decided: `isbn.form.authors` became a block (`label`, `name`, `roles`),
  the shape later words join. The roles line's chevron sits in a 44 square
  at the line's right, so it falls under the trash T146 adds.
- Deviations from the brief: steps 10, 13 and 15 were green on their first
  run and are kept as guards, their mutations run (PR body). Step 4's
  green bound the name with `defineModel`, the brief's shape, which already
  hands on what is typed (step 10); step 12's green groups through
  `authorsInOrder` (step 13); and a `defineModel` the form binds one way
  keeps the typed text across a kind change (step 15), so the form holds
  its authors as a list built once and binds `:name`, not `v-model:name`:
  no case shows the form's own copy before T145 sends the boxes. Step 14's
  rewrite went into step 12's commit, since step 12's green reds the old
  case. The run was continued from a handoff after step 3.
- Left over: the add still sends the answer's authors (T145); boxes inert
  while adding (T146, T147).

## 2026-11-08 — T145 An author is added — done
- Did: the form holds its authors as boxes it sends through the draft
  (`editionOf(isbn13, draft, locale)`); *Ajouter un auteur* adds an empty
  box on `WRITER`, focused; every field takes the `accent` outline on focus.
  S2's two cases un-skipped and green.
- Decided: the boxes are `{ key, name, roles }`, keyed by a counter, and
  the draft gets them as plain `{ name, roles }`; the boxes' block is drawn
  only when it holds one, so the button sits 8 under the label with no
  author.
- Deviations from the brief: step 4's case awaits `flushPromises()` after
  the tap, since the form focuses after `nextTick`. Steps 7 and 8 were
  guards, their mutations run (PR body). Step 6 was red, not a guard. The
  run was continued from a handoff after step 9.
- Left over: the trash and the button inert while adding (T146), the
  roles' list (T147), trimming and empty boxes dropped (T148).

## 2026-11-09 — T146 An author is removed — done
- Did: each `AuthorBox` draws its trash at the right of the name's line,
  *Retirer* and the name, or *Retirer l'auteur*, and emits `remove`; the
  form drops that box, focuses *Ajouter un auteur*, and disables the names,
  the trashes and that button while `adding`. S3 un-skipped and green.
- Decided: the form reaches *Ajouter un auteur* through a function `:ref`,
  not a template ref: the button sits inside the fields' `v-for`, where a
  string ref collects an array (gotcha added). The spec's `trashes()`
  helper, shared by `suggestions` and `disabledOf`, finds the buttons named
  from *Retirer*.
- Deviations from the brief: none. Steps 3, 8 and 9 were guards, their
  mutations run and reverted (PR body). A `fix(frontend)` commit types the
  add's button ref and the spec's trashes after `vue-tsc` refused them.
- Left over: the roles' line enabled while the add runs (T147); the trash
  at the line's right over the chevron, in both schemes, checked by hand by
  Tophe.

## 2026-11-09 — T147 An author's roles chosen — done
- Did: `roleChoicesOf` and `rolesToggled` in `SourceEdition.ts`; each
  `AuthorBox` opens on its roles' line the group *Rôles* of four toggles,
  writes its model `roles`, closes on its line or a `click` on `document`
  outside it; the form binds `v-model:roles`, the lines `disabled` while
  `adding`. S4 un-skipped and green.
- Decided: the open box's outline is `open ? 'border-accent' :
  'border-border'`, not a second colour class beside `border-border`,
  which Tailwind's emission order would settle (found in self-review).
- Deviations from the brief: steps 10 and 12 mount attached to
  `document.body` (`box()` gains an optional `attachTo`): a click on a
  detached tree never reaches `document`, so step 10 stayed red with the
  listener and step 12's mutation could not red (gotcha added). Step 9
  was green on its first run, step 4's green already toggling the list;
  kept as a guard, its mutation run (PR body). Steps 5, 8, 11, 12, 14 and
  16 were guards, their mutations run and reverted (PR body). The run was
  continued from a handoff after the self-review.
- Left over: the checks by hand of the list, the outline and the chevron,
  in both schemes, for Tophe (PR body); names trimmed and empty boxes
  dropped (T148).
