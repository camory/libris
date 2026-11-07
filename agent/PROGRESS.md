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

## 2026-11-06 — T130 The reader's role on every copy, on v0.16.0 — done
- Did: the backend pins `v0.16.0`; every copy it answers names its bookshelf
  with the reader's role, read through `ReaderBookshelf.of`; `DELETE
  /api/v1/copies/{id}` answers through `RemoveCopy`, not found for now.
- Decided: `COPY_OF_ASTERIX_1`'s *Grenier* takes `OWNER`; the add stub of
  `ApiContractTest` answers a new top-level `CONTRACTEER_COPY`, since the
  inline copy ran past detekt's line length; the removal's fixtures are
  `MY_COPY` (the `any()` fallback, the `204`'s id) and `NOT_MY_COPY`.
- Decided: `AddBookToBookshelf` reads its bookshelf's role through
  `checkNotNull(ReaderBookshelf.of(…))`, the reader being an owner there.
- Deviations from the brief: `LookupIsbnForReader` drops its `hasMember`
  filter, `ReaderBookshelf.of` answering null for a non-member; two branch
  commit subjects run past 72 characters (step 3's refactor, step 5's
  un-skip), left as they are since history is not rewritten.
- Left over: T131 removes the copy behind `RemoveCopy`, its result and
  `CopyController`; `S1` and both `S2` stay skipped until then.

## 2026-11-06 — T130 The reader's role on every copy, on v0.16.0 — reworked
- Did: `ApiContractTest`'s `CONTRACTEER_COPY` moved below
  `COPY_OF_ASTERIX_1`, `MY_COPY` and `NOT_MY_COPY` below `TOPHE_AS_OWNER`,
  where the helpers reach them; the review's two code-smell suggestions
  are two lines of `agent/PROPOSED.md`.
- Decided: the reviewer's verdict of 2026-11-06 settled that the stepdown
  rule places a fixture, over the brief's "beside `NOT_MINE`"; its
  Duplicated Code and Middle Man suggestions name a later task and a later
  tidy, so they are proposed, not done; its commit-history remark stays as
  it is, since pushed history is not rewritten.
- Deviations from the brief: the fixtures' place, as the review asks.
- Left over: the two lines of `agent/PROPOSED.md`; T131 as before.

## 2026-11-07 — T131 An owner removes a copy — done
- Did: `CopyRepository` gains `findById` and `delete`, one statement each;
  `RemoveCopy` deletes a copy whose bookshelf the reader owns and answers
  `NoSuchCopy` otherwise; the catalogue's page drops an edition left with
  no copy of the reader's between its two reads, keeping the port's `next`.
- Decided: `RemoveCopy` asks the bookshelf inside one `takeIf` over the
  copy found, a missing bookshelf read as not owned, so the rule stays one
  expression and the two returns detekt allows; `RemoveCopyTest` builds its
  bookshelves through a `salonOf(vararg memberships)` helper, since the
  guest case's inline `bookshelfOf` ran past the line length.
- Decided: `findById` and `delete` sit in the port and its adapters
  beside the methods of their kind, `findById` after `insert`, `delete`
  last, each statement a top-level constant in that order.
- Deviations from the brief: none.
- Left over: nothing; T132 brings the trash to the frontend once T131 is
  deployed.

## 2026-11-07 — T132 The trash on an owned row, on v0.16.0 — done
- Did: the frontend pins `v0.16.0`; every client maps each copy's role
  into `Copy.bookshelf`, a `ReaderBookshelf`; the card draws the trash on
  an owned row, which turns into its question, *Annuler* then *Retirer*.
- Decided: `ReaderBookshelf extends Bookshelf` with `role?:
  MembershipRole` in `src/domain/Copy.ts`, as the brief chose; the trash,
  the cross and the check are `IconButton`s, the cross's `muted` and the
  check's `danger` on the icon, the check at stroke 3.
- Decided: `BookshelvesHolding` holds `asking`, the id of the bookshelf
  whose trash was tapped, `null` until then; the question is a `flex
  items-start` block on `bg`, `p-2`, radius 10, its buttons pulled up by
  `-my-3` so their 44 centre on the sentence's first line of 19.6.
- Decided: step 8's green was a card-wide flag, so step 10 was a red
  case, not a guard; its green keys the question by bookshelf id.
- Decided: `joinedSalon` moved to the spec's top, beside `ownedOnLea`,
  once steps 6 and 10 both built it (a `refactor` commit).
- Decided: the work ran over two runs, a handoff after step 7.
- Fix-up (Tophe's call, after review): `role` is required, as the
  contract has it; the scenario files, `fixture/Books.ts` and the specs
  give each copy a role, and the role-not-given test is gone; the
  reviewer's suggestion applied: one `CopyResponse` and `copyOf` in
  `infra/api`, one `copiesWithIdAndReaderBookshelf` in `EditionShapes`.
- Deviations from the brief: none.
- Left over: T133 makes *Annuler* act, moves the count and un-skips S5,
  which the question kept per bookshelf may already pass; the layout
  and colours checked by hand on the phone, Tophe's.
