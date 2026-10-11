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

## 2026-10-11 — Bookshelves backend scenario tests, with Tophe
- Did: the skipped backend tests of `specs/bookshelves.md`, T072's
  precondition, in `BookshelvesScenarios`, over a scenario DSL in
  `fr.amory.libris.scenario.dsl` that keeps HTTP out of the tests: the
  reader is the subject, a Given step expects success, a When step
  answers a typed value or a refusal (`NOT_FOUND`, `CONFLICT`,
  `invalid(field)`). Skipped, `./gradlew check` is green; S2 and S5,
  un-skipped once, are red at their first bookshelves call.
- Decided: Givens over HTTP only, so adding a member (T073) now comes
  before the order (T074), a name refused (T075) and opening (T076), and
  the four refusals of an add are all T077's, after opening; S5's two
  cases are one parameterized method, which D07 now allows.
- Deviations from the brief: none; no brief.
- Left over: the older scenario classes move onto the DSL in their own
  PR; then T072.

## 2026-10-11 — The older scenario classes on the DSL, with Tophe
- Did: Kind, FastEntry, Catalogue, Bookshelf and Covers scenarios read
  Libris through the scenario DSL, with no HTTP left in a test; `Libris`
  keeps only the DSL's calls; the scenario wiring gives `Libris`, the
  source stubs and `Sources` (forget the requests, no source asked) as
  beans. Same tests, titles, Givens and values, the gate green with the
  same counts as before, save FastEntry's wiring test, dropped.
- Decided: FastEntry S1's strict JSON comparison is one equality over a
  typed card naming every field the contract names, a field it does not
  name no longer failing it; a served cover reads as a `Picture`, its
  format, bytes, how long it may be kept and whether it is immutable; a
  scenario asserts no status code nor content type, the contract's; the
  source stubs are used as they are, already in the sources' words.
- Deviations from the brief: none; no brief.
- Left over: T072.

## 2026-10-11 — T072 Bookshelves on v0.11.0, the thinnest answer — done
- Did: the backend pins `v0.11.0` and serves its six bookshelf operations;
  listing and creating are real, S2 green; opening, the readers, adding a
  member and choosing the default answer through use cases of a constant.
- Decided: the shared not-found answer of `BookshelfController` is one private
  `notFound()`; `NewMemberRequest.toMembership()` turns the request into
  the application type; the add-a-member stubs reuse the contract's ids,
  the default bookshelf's as `CONTRACTEER_BOOKSHELF.id`.
- Deviations from the brief: `CreateBookshelfResult`'s `BlankName` and
  `NameTaken` came in the cycle that maps them (step 8), not with the use
  case (step 6), since no case of step 6 motivates them.
- Left over: the order (T074), the readers and the add (T073), refusals
  (T075, T077), opening (T076) and choosing the default (T078), as the
  backlog has them; D02's wording on web sub-packages is proposed in the
  pull request.
- Ran in two runs: the first handed off after step 8.

## 2026-10-11 — T073 An owner adds a member — done
- Did: `ListReaders` answers every reader stored and `AddMemberToBookshelf`
  stores the new membership through `Bookshelf.withMember` and a whole-
  aggregate `BookshelfRepository.update`; S5 green, both runs.
- Decided: the add reads the bookshelf and the added reader outside the
  transaction, which holds the update alone; `JdbcReaderRepository`'s three
  finders share one private `readerOf`, `JdbcBookshelfRepository`'s insert
  and update one private `insertMemberships`; `ReadersLosingTheRace`, the
  private double of `WelcomeReaderTest`, answers the two new finders from
  the winner it stores once the race is lost.
- Deviations from the brief: none. The commit of step 10 carries an import
  unused until step 11's case, which detekt reports on that commit alone;
  the next commit uses it, and history is not rewritten.
- Left over: the asker's role, an add of a member already there and the
  unknown reader's scenario (T077); no release should be tagged between
  T073 and T077, since any reader knowing a bookshelf's id can add to it.

## 2026-10-11 — T074 The reader's bookshelves in order — done
- Did: `ListReaderBookshelves` answers the reader's default first, then the
  others by name ignoring case and accents; S1 green, un-skipped.
- Decided: the comparison is a private `ignoringCaseAndAccents` of the use
  case, `Normalizer` NFD with the combining marks dropped, then
  `lowercase()`, compared as a plain `String`, so spaces still count as in
  the catalogue's collation; `Collator` at `PRIMARY` was set aside since it
  ignores spaces (measured, item in `gotchas/backend.md`). The default
  leads through the first key of `compareBy`, `id != defaultBookshelfId`;
  the sort is stable, so names equal under the comparison keep the
  repository's order, as the brief asked.
- Deviations from the brief: none. S1's un-skip came at step 4, as the
  brief's test plan orders it, since it is a guard green on un-skip.
- Left over: T076 may extract the comparison for the order of a bookshelf's
  members, as the brief's *Out of scope* says.

## 2026-10-11 — T074 The reader's bookshelves in order — reworked
- Did: `ListReaderBookshelves` imports `NFD` by its bare name and keeps
  `COMBINING_MARKS` at the foot of the file, below its one user; the
  comparison's gotcha item says where it parts from the catalogue's
  collation (ligatures and letters with a stroke).
- Decided: the reviewer's verdict of 2026-10-11 amended nothing. Its
  blocking finding was D10's import rule, unmet. Of its suggestions, the
  constant's place follows D10's stepdown rule, as `CoverController.kt` and
  `IsbnController.kt` do, and Tophe may still settle it at the next review.
  The ligature limit was measured before it was written down, on the JDK
  and on the database collation.
- Deviations from the brief: none.
- Left over: T076 may extract the comparison, as before; whether it should
  also break up ligatures is for its brief, since no spec names such a name.
