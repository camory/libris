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

## 2026-10-11 — T075 A name Libris refuses — done
- Did: a bookshelf's name is a `BookshelfName`, never blank, refused at the
  edge through `BookshelfName.of`; `CreateBookshelf` answers `NameTaken` for
  a name the reader already has; both S3 scenarios green, un-skipped.
- Decided: the name is kept as typed, not trimmed, as the brief says. The
  comparison moved whole from `ListReaderBookshelves` into `BookshelfName.kt`,
  its `œ`/`æ` spelling included; `BookshelfName` orders through it and
  `isSameAs` is that order answering `0`, so S1's order and S3's sameness
  cannot part. `LookupIsbnForReader` keeps sorting by `name.value`, exactly,
  as before: its order is no rule of this task.
- Deviations from the brief: none. Step 6's smallest green, `isSameAs`
  answering `true`, fails detekt on that commit alone
  (`FunctionOnlyReturningConstant`, `UnusedParameter`); step 7 makes it
  whole, history not rewritten, as in T073. The run handed off once, after
  step 9; the next run did step 10 alone.
- Left over: T076 decides whether a reader's display name shares
  `BookshelfName`'s comparison; T081 shows both refusals on the screen.

## 2026-10-11 — T076 A member opens a bookshelf — done
- Did: `FindBookshelf` answers a member the bookshelf and its members, the
  owners first, then the viewers, each by display name ignoring case and
  accents, and anyone else `NoSuchBookshelf`; both S4 cases green.
- Decided: the members are the readers `ReaderRepository.findByIds` answers
  for the bookshelf's memberships, each with `Bookshelf.roleOf`; the order
  is `MembershipRole.order` then `IgnoringCaseAndAccents` over the display
  name, applied in the use case. `ReadersLosingTheRace.findByIds` answers
  the winner once the race is lost and only for its id, as its other
  finders do.
- Deviations from the brief: step 9's green copied the folding into
  `FindBookshelf.kt`; the refactor commit that follows removed the copy
  when it moved the folding to `IgnoringCaseAndAccents`, so no commit
  widens a visibility. Otherwise none.
- Left over: the refusals of an add, which read the members back (T077);
  the screen of a bookshelf (T082).

## 2026-10-11 — T085 Bookshelves and members in no promised order — done
- Did: `ListReaderBookshelves` and `FindBookshelf` sort nothing and answer
  in the repositories' order; `MembershipRole` is `OWNER` and `VIEWER` bare;
  the folding is private to `BookshelfName.kt` again, `IgnoringCaseAndAccents.kt` gone.
- Decided: the three guards on `BookshelfName.isSameAs` (`œ`, `æ`, a space
  that counts) came first, each committed before its mutation was run, so
  the folding was never left unproven while the order cases left. The
  folding's shape is T075's, without the companion's comparator.
- Deviations from the brief: none.
- Left over: the authors and a card's bookshelves (T087, after T086);
  narrowing `ListReaderBookshelves` to a `ReaderId`, as the brief's
  *Risks* note, is not proposed: nothing asks for it yet.

## 2026-10-11 — T086 The card and a catalogue row sort their authors and bookshelves — done
- Did: `nameOrder(locale)` in `domain`, an `Intl.Collator` ignoring case and
  accents; `authorsInOrder` and `bookshelvesHolding` group and sort through
  it; the card and a catalogue row take both lists from them.
- Decided: the card's one-line test compares `roles.join()` alone, the roles
  arriving in role order from `authorsInOrder`, so the card's own sort of a
  set's codes left. The card's rows iterate `{ bookshelf, count }`, the
  shape the domain answers, keyed on the bookshelf's id as before.
- Deviations from the brief: none.
- Left over: T087 drops the backend's order once this is deployed; the
  examples *Erckmann, Chatrian* of U06 and `specs/kind.md` are proposed in
  the PR to read *Chatrian, Erckmann*, for Tophe to settle.

## 2026-10-11 — T087 Authors and a card's bookshelves in no promised order — done
- Did: `Contributions.of` keeps one contribution per author and role and
  orders nothing; `ContributionRole` is four bare entries; `LookupIsbnForReader`
  answers a card's copies in the repositories' order.
- Decided: `Contributions` holds a `Set` built from the distinct list, so it
  stays equal whatever the order given, as the brief's *Risks* measured;
  the guard on that equality came first, committed before its mutation ran.
- Deviations from the brief: none.
- Left over: the scenario comparisons of `authors` in order, proposed in
  `agent/PROPOSED.md` for Tophe; merging is not deploying, the backend
  drops the order once Tophe deploys it.

## 2026-10-11 — T077 An add Libris refuses — done
- Did: `AddMemberToBookshelf` answers a viewer and a stranger
  `NoSuchBookshelf` through `isOwnedBy`, and a reader already a member
  `AlreadyAMember` through `hasMember`; `Bookshelf` refuses a reader twice.
- Decided: every refusal is decided from the reads, before the
  transaction, and writes nothing. The checks run owner, then known
  reader, then member, all in one `when`.
- Fix-up on Tophe's ask: `withMember` answered `Bookshelf?`, `null` for a
  reader already a member; it answers a `Bookshelf` again, the `init`
  requires one membership per reader, and the use case asks `hasMember`
  before it.
- Fix-up on Tophe's ask: S6's two ordered comparisons of the members
  compare in any order, as S1 and S4 do; the PROPOSED item goes.
- Left over: the frontend's add and its refusals (T083).

## 2026-10-12 — T078 A reader chooses their default — done
- Did: `ChooseDefaultBookshelf` makes a bookshelf the reader owns their
  default, the reader written through the new `ReaderRepository.update` in
  one transaction; a viewer, a stranger and an unknown id answer `NoSuchBookshelf`.
- Decided: the use case reads like `AddMemberToBookshelf`, the bookshelf
  read through `isOwnedBy` and its outcomes in one `when`; the update in a
  private `chosen`. `ReadersLosingTheRace.update` throws
  `UnsupportedOperationException`, since `WelcomeReader` never calls it.
- Deviations from the brief: steps 4 and 6 each first gave the use case the
  constructor its test calls, the old body kept, so the red read was the
  assertion's, not a compile error; that intermediate state was never
  committed.
- Fix-up on Tophe's ask: `ReadersInMemory` keyed by id, so an update keeps
  the reader in its place; `ChooseDefaultBookshelfTest` stores Marc too; the
  gotcha on the fake's order goes.
- Left over: the frontend's pin and home page (T079), the star (T084).

## 2026-10-13 — T079 The home page lists the reader's bookshelves — done
- Did: the home page reads *Libris*, the greeting, then *Mes bibliothèques*:
  a `BookshelfRow` per membership in `membershipsInOrder`, skeleton rows
  while they come, the loading sentence when Libris does not answer.
- Decided: the four operations of `FetchBookshelvesApi` no screen uses yet
  reject with an `Error` naming the operation, as the brief says.
- Decided: the row's muted line is one catalogue entry, `{role} · par
  défaut` on the default; the label is an `h2` in the `label` step.
- Decided: the view computes the list once both the reader and the
  memberships are there; the skeleton shows until then, the sentence as
  soon as the memberships fail.
- Deviations from the brief: the client sends `Accept: application/json,
  application/problem+json`, the header every request of this tree carries.
- Deviations from the brief: the list is named by `aria-label`, not
  labelled by the visible label: `aria-labelledby` throws on the detached
  trees the specs and the scenarios mount.
- Deviations from the brief: step 6 keeps the state before `list()` as
  `before` in the Given; step 14 asserts before and after `flushPromises`.
- Deviations from the brief: the handoff's Prettier reformatting of
  `CoversScenarios.spec.ts` reverted in its own commit, its `open()` kept.
- Handoff: the first run did steps 1 to 7 (scenarios read red first, put
  back skipped); this run did steps 8 to 17.
- Left over: the chevron and the row's link (T082), the star (T084), the
  plus (T080); the spec's U08 and U03 amendments, proposed in the PR.

## 2026-10-13 — T079 The home page lists the reader's bookshelves — reworked
- Did: *draws skeleton rows under the label while the bookshelves come*
  keeps what the reader sees before `flushPromises` in its Given and
  asserts before and after in one Then; *has no link to add an ouvrage*
  takes Given / When / Then; one card holds the list or the skeleton rows.
- Decided: the reviewer's verdict of 2026-10-13 amended nothing; its two
  blocking findings are the layout D07 asks, its card suggestion applied.
  The `normalised` helper stays in both specs, as the suggestion itself
  allows: a shared one would sit in `src/fixture`, which the brief keeps
  unchanged.
- Deviations from the brief: none.
- Left over: the title case's missing markers, in `agent/PROPOSED.md`.

## 2026-10-13 — T080 A reader creates a bookshelf — done
- Did: the plus *Nouvelle bibliothèque* right of *Mes bibliothèques* opens
  `NewBookshelfRow` as the list's last row; *Créer* creates the bookshelf
  through `useCreateBookshelf`, and the answer joins the rows in S1's order.
- Decided: the view keeps the memberships created on the page in a list of
  its own and hands it to `membershipsInOrder` with the listed ones; the row
  goes on `created` only, so a problem answer leaves the form as it is.
- Decided: the view spec's busy case runs over `LibrisNotAnswering`, a
  subclass of `FakeBookshelvesApi` declared in the spec, passed to
  `mountHomeView` as an optional last argument.
- Deviations from the brief: `@lucide/vue` and the provided stroke arrived
  in step 9, not 13: `Check` is the first icon drawn.
- Deviations from the brief: the field carries an `aria-label` holding its
  placeholder's word; a placeholder alone gives `getByRole("textbox")` no
  name.
- Deviations from the brief: the first commit, the domain stub with step
  1's case, is typed `test(frontend)`.
- Handoff: the first run did steps 1 to 13 (S2 read red first, put back
  skipped); this run did steps 14 to 18.
- Left over: the refusals, the unavailable case and the form's `catch`
  (T081); the chevron and the row's link (T082); the star (T084).
