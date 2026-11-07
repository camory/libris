# Libris — Progress log, Remove a copy

The entries of `agent/PROGRESS.md` of removing a copy, T130 to T137,
done 2026-11-07, moved here on 2026-11-07 once what a run needs of them
was in `agent/gotchas/`. Nothing is appended here.

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

## 2026-11-07 — T133 The count on its own line, Annuler, one question — done
- Did: the card's bookshelf row draws *Dans* and the name, then, past one
  copy, *2 exemplaires* on a line of its own; every row is at least 44 tall,
  its words 12 under its top; the cross *Annuler* puts the row back.
- Decided: S4 and S5 were un-skipped as the first commit, their red read
  before any unit test (S4 red on *Dans Bibliothèque de Léa* not found, S5
  already green), as the implementer's rules order, the brief listing them
  last as steps 5 and 6.
- Decided: the row is `flex items-start`; the book icon takes `my-2.75`
  (a 44 box, centred at 22) and the words column `py-3`, so the icon and
  the trash, a 44 `IconButton` at the top, centre on the first line of
  19.6; the absence row takes the same shape.
- Decided: the counting case reads the card's text through `card()`'s
  `wrapper.text()` beside `within(wrapper.element)`, since `screen()`
  searches inside the `article` root and cannot read the card's whole text.
- Decided: `secondOnLea` moved to the spec's top, beside `onLea`, once
  steps 1 and 3 both built it (a `refactor` commit).
- Decided: Prettier puts edition-page S1's two expected rows on one line,
  still the first row's change alone.
- Deviations from the brief: none.
- Left over: T134 makes *Retirer* send; the 44, the 12 above the words and
  the icons on the first line, in both schemes, checked by hand by Tophe.

## 2026-11-07 — T134 Retirer removes one copy, on the ouvrage's page — done
- Did: `FetchCopiesApi` sends the removal, its port in `LibrisPorts`; the
  card's *Retirer* hands up one copy of the row, a spinner in its place
  meanwhile; the ouvrage's page removes it and asks the ouvrage again.
- Decided: S1 was un-skipped first and read red before any unit test; it
  reds on `findByText("Dans Bibliothèque de Léa")`, the asking row
  drawing no *2 exemplaires* for the brief's `waitFor` to wait on.
- Decided: `BookshelvesHolding` picks the first copy of the asking row's
  bookshelf from `copies`; `ownedSalon` became a file constant of
  `SourceEditionCard.spec.ts` once steps 7 to 10 all built it.
- Decided: `useRemoveCopy` answers `removed` whatever the port answers and
  the page asks again unconditionally: the problem and the rejection are
  T135's, no branch without its test.
- Decided: `EditionPageView.spec.ts`' mount helper takes the copies' port
  third, rejecting by default; step 13 reads the asked id with
  `expect.toBeOneOf` over the row's two copies.
- Decided: the work ran over two runs, a handoff after step 9.
- Deviations from the brief: step 1's red is on the name's row, not on
  the `waitFor` of *2 exemplaires* (above).
- Fix-ups by the interactive assistant, on Tophe's call after his Pixel
  try: the page blinked, `find` dropping the card for the skeleton; the
  page now asks again through `refresh`, no `loading` step, the spinner
  turning until the answer comes, and new copies close the question; the
  reviewer's suggestion taken, every trash disabled while removing.
- Left over: T135 (problems, S6, S7), T136 (the last copy, S2), T137 (the
  lookup card, S8); the spinner against `3-removing.jpg` and the page
  after the removal against `4-removed.jpg`, both schemes, Tophe's.

## 2026-11-07 — T135 Libris unavailable, or the copy already gone — done
- Did: `useRemoveCopy` answers `unavailable` to a rejection or a refusal
  and takes the not-found answer as a removal; the asking row draws the
  message under its question, ready for another try; S6 and S7 run.
- Decided: S6 was un-skipped first, red on the sentence with the
  *Unhandled Rejection* the brief measured, before any unit test.
- Decided: `remove` reads the answer through a private `isGone`, `true`
  for `removed` and `/problems/not-found`, `false` for any other problem
  and for a rejection; `afterRemoval` runs only when it answers `true`.
- Decided: how the card tells an answered question from a new one:
  `BookshelvesHolding` keeps a `failed` ref, set from each change of the
  `removalFailed` prop and cleared by the cross and every trash. A first
  try, `failed` set only after a tap on *Retirer*, reddened step 7, whose
  case sets the prop with no tap.
- Decided: the asking row became a `bg` block holding the question's
  flex row, then the message (`pt-2`, icon and sentence `gap-2`);
  `font-normal` moved from the sentence to the block.
- Deviations from the brief: S7's mutation (not-found read as
  unavailable) reds on `findByText("Dans Bibliothèque de Léa")`, the
  question still open, before the assertion on the sentence.
- Left over: T136 (the last copy, S2), T137 (the lookup card, S8); the
  message against `5-not-removed.jpg`, both schemes, Tophe's.

## 2026-11-07 — T136 The last copy removed, back where the page came from — done
- Did: the ouvrage's page, its last copy removed or answered not found,
  goes back through the history when it was opened from another screen and
  replaces its entry with `/catalogue` when opened from its address; S2 runs.
- Decided: S2's two cases were un-skipped first, each its own commit, both
  red on the pathname before any unit test; step 3's green turned them both.
- Decided: the view keeps the id `remove` was handed (`removeCopy`) and
  counts the ouvrage's other copies, every bookshelf and role, in its
  `afterRemoval`; `useRemoveCopy` and `useFindCatalogueEdition` unchanged.
- Decided: the back is awaited through a one-shot `router.afterEach`, so
  `afterRemoval` ends, and the spinner stops, once the page is left.
- Decided: the spec gained `askingToRemove(router, catalogue, copiesApi)`
  and `routerOn(...paths)`; `LibrisRecordingAsks` takes its answer.
- Deviations from the brief: step 9 as first written stayed green under
  its mutation: the asking row reads the question, not *Dans Bibliothèque
  de Léa*, so the `waitFor` passed at once; reworded to `flushPromises`
  then the pathname, no text of Léa's, *Dans Salon*.
- Deviations from the brief: one commit subject runs 74 characters; left,
  history not rewritten, the PR title being the squash commit.
- Left over: T137 (the lookup card, S8); on the Pixel, the last copy
  removed from an ouvrage opened from the catalogue and from a series'
  page, back on that screen, the back button not returning, Tophe's.

## 2026-11-07 — T136 The last copy removed, back where the page came from — reworked
- Did: `EditionPageView.vue` reads from the top down (`afterRemoval`, then
  `copiesLeft`, `openedFromAnotherScreen`, `back`, then `removeCopy`), the
  history's check named `openedFromAnotherScreen()`; the spec's helpers
  come as reached, `seen`, `routerOn`, `askingToRemove`; PR retitled.
- Decided: the reviewer's verdict of 2026-11-07 settled nothing new: its
  three blocking findings were the brief's D10 rules unmet (the title's
  72 characters, the stepdown order in the view and in its spec); its
  suggestions applied: the check named, the 76 corrected to 74 here and in
  the PR body, and the `let removedId` with its `removeCopy` wrapper
  proposed for a later task in `agent/PROPOSED.md`.
- Deviations from the brief: none.
- Left over: as before, T137 (the lookup card, S8) and the check by hand
  on the Pixel, Tophe's.

## 2026-11-07 — T137 Removing from the lookup screen — done
- Did: the lookup screen's trash removes one copy as the ouvrage's page does;
  the card stays and drops the copy, the *Added* state ends and the button
  *Ajouter à ma bibliothèque* comes back; S8's two cases run.
- Decided: S8's two cases un-skipped first, each its own commit, red as the
  brief measured; they turned green at steps 3 and 4.
- Decided: `useRemoveCopy` held in a `shallowRef` rebuilt by `copyRemoval()`
  on each lookup, its `afterRemoval` acting only while it is still the
  current one, so a late answer leaves the next card alone.
- Decided: `dropRemovedCopy` keeps the shown copies (the lookup's and the
  added one) less the removed id and rebuilds `addBookToBookshelf`; after an
  add it makes the card the house's: `edition` the sent one, `covers` the
  sent candidate, `held` true, `editing` false.
- Decided: the work ran over two runs, the first handing off after step 14
  and its refactor; the second ran the gate, read the diff and finished.
- Deviations from the brief: step 4 also counts one trash left, since "no
  *Dans Chambre*" held while the row still asked.
- Deviations from the brief: step 12's mutation reds on
  `getByText("Dans Bibliothèque de Léa")`, the question still open, before
  the assertion on the sentence.
- Left over: the two lookup cards against `6-lookup-card.jpg` and
  `7-lookup-card-emptied.jpg`, and the spec's *Done* on the Pixel, Tophe's.
