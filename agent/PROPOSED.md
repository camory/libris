# Libris — Proposed

> Changes that could be a task today: a bug with how it is reproduced, or a
> duplication or smell that names its files. One bullet each, three lines
> at most, with the date and where it was found, under *Bugs*, *Tidy* or
> *Loop, CI and gate*. Nothing else goes here: a note that waits for a day
> that has not come is not written, a product question goes to the pull
> request's **Follow-ups**, a fact learned to `agent/PROGRESS.md`, a tool or
> version to watch to `agent/gotchas/`.
>
> At each spec's Done walk, Tophe reads the file with Claude and promotes or
> deletes every entry. A promoted entry moves to *Promoted*; the planner
> plans that section as the phase *Tidy*, each entry prefixed with the ID
> of its task, and the run that ticks a task removes its entries. An entry
> no run may do, the loop, CI and the gate, or Tophe's scenario tests,
> moves to *With Tophe* instead and leaves when it is done in a session.
> Nothing else here is picked up by a run.

## Tidy

- Frontend: `useListSeriesNames` and `useListAuthorNames` differ only by
  their port, the latest-ask guard included; one composable over a
  `() => Promise<string[]>` would serve both (found on T178, 2026-11-14).
- Frontend tests: `later()`, a promise settled by hand, is written in the
  specs of `useListSeriesNames`, `useListAuthorNames`, `useSearchCatalogue`
  and in `SearchScenarios`; one generic in `src/fixture` (T178, 2026-11-14).
- Backend tests: `EditionDetailsRequestTest` and `NewBookRequestTest` assert
  the codes `blank` and `not-an-isbn`, which no operation of the contract
  enumerates; assert the refusal and its field only (T181, 2026-11-15).

## Promoted

- T182 Frontend: the failure message, `CircleAlert` and a sentence in `danger`,
  is drawn by hand in fourteen components and views (`DockedButton`,
  `EditionForm`, `IsbnView`, `HomeView`…); one component of
  `ui/components` with its own spec would draw it once (found on T098 and
  T124, 2026-10-16 and 2026-11-03).
- T183 Frontend: `IconButton` draws the icon button in `IsbnView`,
  `EditionPageView`, `AuthorBox`, `SearchField` and `BookshelvesHolding`;
  `HomeView`, `BookshelfView`, `ReaderChoice`, `BookshelfRow` and
  `NewBookshelfRow` still write its box by hand. Move them onto it, their
  tests unchanged (found on T128, 2026-11-03).
- T184 Frontend: `SeriesRow.vue` copies `CatalogueRow.vue`'s cover block: the
  48 × 74 box, the `img` that falls back on `error` and the `BookOpen`
  stand-in. One cover component with its own spec (found on T097,
  2026-10-16).
- T185 Frontend: `useFindBookshelf`, `useFindCatalogueSeries` and
  `useFindCatalogueEdition` map a find's answer alike (a rejection or any
  problem but not-found and validation to `unavailable`), and
  `SeriesPageView` and `EditionPageView` pick their failure's words with the
  same `computed`; one mapping would serve the three (found on T115,
  2026-10-23).
- T186 Frontend: `EditionPageView` and `IsbnView` keep the removed copy's id in a
  mutable `let removedId`, set by a wrapper before `remove` and read in
  `afterRemoval`; were `useRemoveCopy` to hand the id to `afterRemoval`,
  both `let`s and both wrappers would go (found on T136, 2026-11-07).
- T187 Frontend: `FetchCatalogueApi`'s `browse` and `search` read a page alike;
  one private method would serve both (found on #407, 2026-11-12).
- T188 Frontend tests: `EditionForm`, `ReaderChoice` and `AuthorBox` call
  `scrollIntoView?.(…)` because jsdom lacks it; a vitest `setupFiles` entry
  defining it as a no-op would let them call it plainly, the jsdom gotchas
  rewritten as one (found on T124, 2026-11-03). `HomeView.spec.ts`
  *renders the French title from the messages* has no Given / When / Then
  markers (found on T079, 2026-10-13).
- T189 Backend: `BrowseCatalogue`, `FindCatalogueSeries` and `HeldEditionReads`
  each build the reader's bookshelves by id with the same chain,
  `findByMember(readerId).mapNotNull { ReaderBookshelf.of(it, readerId) }
  .associateBy { it.id }`; one function would hold it (found on T130,
  2026-11-06).
- T190 Backend tests: every `@WebSliceTest` class names every use case of `web`
  in its `@MockitoBean`, and the identical lists are what let
  `ProblemAdviceTest` and `SecurityConfigTest` share a context; declaring
  the mocks once on `@WebSliceTest` would keep one list, if `@MockitoBean`
  is honoured on a meta-annotation (found on T020 and T142).
- T191 Backend tests: the top-level fixtures sit above the class in
  `LookupIsbnForReaderTest`, `LookupEditionByIsbnTest`, `AwaitedCoverTest`
  and `ProblemAdviceTest`; move them below the class and its helpers
  (found on T093, 2026-10-15).
- T191 Backend tests: delete `ProblemAdviceTest`'s *a bookshelf id that is not a
  uuid is a validation problem*, which the contract case `auto: path 'id'
  type mismatch` proves since `v0.9.0` (Tophe, 2026-11-14).

## With Tophe

- Backend: `BnfSource` builds its cover candidate on the constant
  `https://catalogue.bnf.fr/couverture` while `libris.sources.bnf-covers-url`
  exists; build it on the setting, with `FastEntryScenarios` reading the
  stub's address, a scenario edit that is Tophe's (Tophe, 2026-11-14).
- Backend tests: `SummaryScenarios` boots a context of its own for its
  `spring.flyway.target=12`; migrating to 12 through a `Flyway` built from
  the context's configuration, as its second half already does, would let
  it share the scenario context. Tophe's scenario (Tophe, 2026-11-14).
- Frontend: the ports answer `{ outcome: "problem"; type }`, so a
  `/problems/…` slug crosses into the application layer and every fake and
  view speaks it. The adapter alone should turn the status into an outcome
  in the spec's words (`unknown`, `unavailable`, `refused`…), the views map
  outcomes to messages, and the `Fetch*Api` specs assert outcomes; the
  failure Givens of thirteen scenario test files change with it, Tophe's
  (found reviewing contract v0.8.0 with Tophe, 2026-09-30).
- Contract: `Problem.type` in `camory/libris-api` is described as "a slug
  the client switches on", while since T060 both clients read the HTTP
  status and fall back on `type`; reword it at the next contract release
  (found on T060, 2026-10-06).
- Loop: `sandbox_down` runs `compose down`, which leaves the one-off `run`
  container, so an interrupted loop leaves the agent running and spending.
  Remove the run containers too (`docker compose rm -sf agent`, to verify),
  with a case in `agent/test-loop.sh` (found 2026-09-08).
- Loop: resume after the usage window resets. A role whose JSON ends with
  `is_error: true` and "You've hit your session limit · resets …" leaves no
  report and the loop exits 6 with the task half done. Sleep until the
  reset and rerun the role once with `claude -p --resume <session_id>`,
  with a case in `agent/test-loop.sh` (seen 2026-09-08).
- Gate: nothing runs Prettier, so `src/domain/Copy.ts`,
  `src/scenario/CoversScenarios.spec.ts` and
  `src/ui/components/SourceEditionCard.spec.ts` are unformatted on `main`
  and `npm run format` rewrites them. Add a `prettier --check` to the
  frontend gate and format the three (found on T032, decided 2026-11-14).
