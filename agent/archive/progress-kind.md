# Libris — Progress log, Kind

The entries of `agent/PROGRESS.md` of the kind feature, T031 and T032,
done 2026-09-19, moved here on 2026-09-21 once what a run needs of them
was in `agent/GOTCHAS.md`. Nothing is appended here.

---

## 2026-09-18 — T031 The kind of a scanned ouvrage — done
- Did: `domain/Kind.kt` holds `BOOK, BD, MANGA`; `internal fun kindOf` in
  `BnfSource.kt` reads the form of contents of field 105 `$a` and the language
  of 101 `$c`; `SourceEdition` carries `kind: Kind?`, the merge takes the first
  source that names one, `IsbnResponse.kind` is non-null and `responseOf`
  writes the `BOOK` default. The backend pin is `v0.5.0`; the four scenarios of
  `KindScenarios` are un-skipped and green, gate green, 106 tests.
- Decided:
  - **The default lives in one place.** `SourceEdition.kind` is nullable with
    no default, so every construction site had to state its kind and the
    compiler listed them; `edition.kind ?: BOOK` in `responseOf` is the only
    `BOOK` the run writes, and *S4 No record says* is its end-to-end proof.
    Undone on review, see the fix-up below.
  - **The rule reads the slice, not the whole field.** `COMIC_STRIP !in
    codedData.orEmpty().drop(4).take(4)` mirrors `publicationYearOf`'s slice,
    keeps a `t` elsewhere in the coded data out of the answer, and gives the
    no-105 record `BOOK` without a guard of its own.
  - **The three manga languages are the BnF's codes.** Field 101 `$c` answers
    `jpn`, `kor`, `chi`; the `LANGUAGES` map that turns `fre` into `fr` serves
    the `language` field only and is not in the rule's way.
- Deviations from the brief: two, both small. Cycle 3's `SourceEdition`
  parameter with no default reds `ApiContractTest` at compile time, so
  `kind = MANGA` on the `ONE_PIECE_1` stub was written there instead of in
  cycle 4 as the test plan scheduled; the case itself stayed red until cycle 4.
  And the gate found `FastEntryScenarios > S1`, which the brief lists under
  *Not changed*: it compares the whole body strictly, so it gains the line
  `"kind": "MANGA"` — the record it stubs is the manga, the comparison stays
  `STRICT` and the case asserts one field more than before.
- Left over: nothing of the task. T032 carries the field to the frontend;
  nothing stores a kind yet, which the bookshelf brings.
- Fix-up on review with Tophe: no kind is ever unknown, since everything the
  rule does not mark is a book, so `SourceEdition.kind` is non-null and no
  default exists anywhere. Open Library answers `BOOK`, the merge takes the
  first source's kind as it takes the title, the controller copies it.

## 2026-09-19 — T032 The card in the words of its kind — done
- Did: the frontend pin moved to `v0.5.0`, `SourceEdition` gained
  `kind: Kind` read straight off the answer by `FetchIsbnApi`, and the `fr`
  catalogue grew a series pattern and a block of four role words per kind, so
  `SourceEditionCard` builds `isbn.card.series.<kind>` and
  `role.<kind>.<role>` and holds no French word and no branch on a kind. Its
  `authorLines` now groups the authors by name, compares their whole sets of
  roles and answers either one line of names or a line per author with its
  words. Eleven cycles, `S1 A manga` un-skipped, gate green: 14 files, 111
  tests, no skip.
- Decided:
  - **The grouping branch is a rendering shape, not two computeds.** The
    computed answers `{ name, roles }` with `roles: null` for the shared-set
    line, and the template picks `i18n-t` or a plain `<p>` on it. One list,
    one `v-for`, and the case *shows no author line…* falls out of the empty
    list with no guard.
  - **The canonical key is the sorted role codes, the display order the
    answer's.** A mutation check proved the point: dropping `.sort()` left
    all 21 cases green, so the run added *reads the same roles in another
    order as the same set*, watched it red against the unsorted variant, and
    put the sort back — 22 green. The words still read *scénario, dessin* in
    the order the BnF gave them.
  - **Every kind carries its full set of words, as the brief asked.** The
    reviewer is asked to weigh the six repeated words (*couleurs* and
    *traduction* three times, the manga's pattern repeating the livre's)
    against a shared entry that the first divergence would have to split.
- Deviations from the brief: none. The test plan's order held cycle for
  cycle, including the pin red opening the task and Prettier over
  `vitest.global-setup.ts` alone at the end.
- Left over: nothing of the task. Two follow-ups went to `agent/PROPOSED.md`
  — the grouping moving to `domain` the day a second screen names authors,
  and the two files still unformatted on `main`. Nothing stores a kind yet;
  the bookshelf brings it over `v0.6.0`.
- Fix-up on review with Tophe: a role an answer lists twice for one author
  is kept once while grouping, so the key compares sets and the words never
  repeat; its case added, 112 tests.
