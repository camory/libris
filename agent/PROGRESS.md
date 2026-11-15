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

## 2026-11-12 — Search done, with Tophe
- Did: Tophe checked the *Done* of `specs/search.md` on staging. The spec
  is done; the Search section (T164–T176) of `agent/TASKS.md` is one line
  under *Done*.
- Decided: its entries moved to `agent/archive/progress-search.md`, as
  the phases before it did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

## 2026-11-14 — T177 a lookup Libris does not answer no longer hangs — done
- Did: `FetchIsbnApi` takes `onUnauthenticated` and calls it on a `401`,
  then rejects; `bootstrap` hands it the callback it hands `FetchMeApi`;
  `IsbnView`'s `search()` reads a rejected lookup as no answer.
- Decided: the catch is `lookUp(isbn13).catch(() => null)` followed by an
  early return, so `searching` falls back on the next line whatever the
  answer and nothing is drawn under the field (no card, no form, no
  message), as the brief asks; the `finally` the PROPOSED entry named
  would have left the rejection escaping the click handler.
- Deviations from the brief: none.
- Left over: what the lookup screen says when Libris does not answer
  (PRD §4.7 *Still to spec*), in the PR's Follow-ups for Tophe; a `401` on
  the other clients, each its own bug; the D06 and D07 sentences proposed
  in the PR body; a lookup with the network off and one with an expired
  session, left to Tophe on the Pixel.

## 2026-11-14 — T178 the form suggests the names of the latest lookup — done
- Did: `useListSeriesNames` and `useListAuthorNames` number each `list()`
  call and write `names` only for the latest one, its answer or its
  rejection's `null`; an earlier answer or failure writes nothing.
- Decided: the rejection is read as `null` through `.catch(() => null)`
  before the latest-ask check, as `useSearchCatalogue`'s `pageOf` does,
  so one check covers an answer and a failure alike; the spec-local fakes
  are named `LibrisAnsweringInTurn`, as `useCreateBookshelf.spec.ts`'s.
- Deviations from the brief: none.
- Left over: nothing; the two callers, `IsbnView` and `EditionPageView`,
  are unchanged and read the fixed `names` as before.

## 2026-11-14 — T179 a tome of digits alone in the BnF's record — done
- Did: `tomeOf` reads a `200 $h` that is digits alone, the whole subfield
  as written, as that tome, after `tome N`; the BnF answer of
  9782845808461 is recorded and read whole by `BnfSourceTest`.
- Decided: the bare form is a second clause of `tomeOf`'s elvis, a regex
  `\d+` matched against the whole `$h` then `toIntOrNull()`, so `tome N`
  keeps its first place and a `$h` past an `Int` is no tome.
- Deviations from the brief: none; the recording matched every value the
  brief measured while planning.
- Left over: 9782845808485 (tome 16) reads by the same rule, unrecorded;
  the other forms of `$h` (`t. 14`, `14 bis`) stay out until a record
  shows one.

## 2026-11-15 — T180 the proxy's headers read whole — done
- Did: `RemoteIdentity.of` answers no identity for a blank `Remote-User`,
  as for a blank `Remote-Email`, and decodes `Remote-User`, `Remote-Email`
  and `Remote-Groups` from UTF-8 as it decoded `Remote-Name`.
- Decided: every header goes through one private `textOf`, decoded then
  refused when blank, so blankness is judged on the decoded text; the
  groups split inside `authoritiesOf`, which now takes the header's text.
  The test's `asTomcatReads` builds a header as Tomcat reads it, for the
  four UTF-8 cases.
- Deviations from the brief: none.
- Left over: the check of stored usernames outside ASCII before the
  deploy, in the PR's Follow-ups for Tophe.

## 2026-11-15 — T181 an add out of the contract's bounds is refused — done
- Did: `EditionDetailsRequest.validate` refuses a language that is not two
  lower-case letters (`not-a-language`), and a publication year, a page
  count or a volume number below one (`not-positive`, the last on `series`).
- Decided: the four checks sit in a private `outOfBounds`, its errors after
  the blank ones, since a fourth check in `validate` crossed detekt's
  cyclomatic threshold of 15; a below-one number reads through a private
  `Int?.isBelowOne`. The edit's `PUT /api/v1/books/{id}` refuses the same
  values, validating through the same class.
- Deviations from the brief: `baggyAndHisMen` gains `volumeNumber` alone;
  the language, the year and the page count are set with `copy(…)`, since
  three more parameters would cross detekt's `LongParameterList` threshold
  of six for a function. The split of `validate` landed in step 5's commit,
  not a refactor of its own, since detekt failed that green without it.
- Left over: nothing.

## 2026-11-15 — T181 an add out of the contract's bounds is refused — reworked
- Did: the five bound refusals of `EditionDetailsRequestTest` assert the
  refused field alone, through `shouldBeInstanceOf<Refused>()`, no longer
  its code; the two accepted guards expect `DETAILS_OF_BAGGY_AND_HIS_MEN`
  with `copy(…)`, a fixture below the class.
- Decided: the reviewer's verdict of 2026-11-15 settled that a test asserts
  no code the contract does not enumerate, which the brief's acceptance
  criteria and test plan asked; the codes stay in the production code as
  documentation. The first case, *a body of every field is accepted…*,
  keeps its own literal: the brief wants the existing cases unchanged.
  Each case's red and each guard's mutation was run again on the new
  assertions and read red.
- Deviations from the brief: the refusal cases assert `Refused` and its
  field, not the whole answer, as the review asks.
- Left over: nothing; the mixed green and refactor of `72dfdb08` stays in
  the history, which is not rewritten.

## 2026-11-15 — T182 the failure message drawn once — done
- Did: `FailureMessage` (`ui/components`, prop `sentence`) draws the alert
  and the sentence of a failure; the fourteen components and views that
  wrote it by hand draw it, each keeping its condition and spacing.
- Decided: the moves were done by a throwaway script that read each hand
  block and wrote the tag, its common classes dropped and the rest kept on
  the tag, then Prettier; the catalogue's block, whose bound `:class` holds
  a `>`, was moved by hand. Subjects say each screen's name ("the lookup
  screen draws FailureMessage").
- Deviations from the brief: none.
- Left over: a look at the home page's *Erreur lors du chargement* and a
  refused field of the form on the running app, left to Tophe in the PR.

## 2026-11-15 — T183 every icon button on the one icon button — done
- Did: the plus of the home page and of a bookshelf, the star of a home
  row, the check of a new bookshelf and the cross, the crown and the check
  of the choice of a reader are drawn by `IconButton`, one commit per file.
- Decided: nothing; the brief settled the cross's `text-muted` on `<X>`
  and the `shrink-0` the four buttons gain.
- Deviations from the brief: none.
- Left over: a look at the home page and a bookshelf's members on the
  running app, and the icons' sizes (24 here, 22 on the other callers),
  left to Tophe in the PR.

## 2026-11-15 — T184 one cover for the catalogue's rows — done
- Did: `RowCover` (`ui/components`, props `url` and `alt`) draws a row's
  cover, its picture, the fall-back on error and the book icon; the
  catalogue row and the series row draw it, one commit each.
- Decided: the failure's ref is `failed` in the component, the rows'
  `coverFailed` being the row's word for it; step 1's green wrote
  `:src="url ?? undefined"`, since `vue-tsc` refuses `null` there, and
  step 2's `v-if` dropped it.
- Deviations from the brief: step 2 is two commits, its red case committed
  alone by mistake, then its green; the first subject runs to 73
  characters. History is not rewritten; both are declared in the PR.
- Left over: a look at a row with a cover and one without, in the catalogue
  and the series list, on the running app, left to Tophe in the PR.
