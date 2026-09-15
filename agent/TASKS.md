# Libris — Task backlog

> Ordered. The loop takes the **first unchecked** task. Keep tasks small enough
> for one PR a human reads in ten minutes. Each task states its acceptance
> criteria; "tests pass" is implied everywhere (see `CLAUDE.md`).
>
> Every phase is one feature spec in `specs/`. The planner derives the tasks
> from the spec's scenarios, each task line citing the scenarios it realises,
> and the phase ends when the spec's *Done* is checked. A done phase is one
> line under *Done* below; its story stays in `specs/`, `agent/briefs/` and
> `agent/PROGRESS.md`.
>
> Human steps are not tasks. A task that needs one states it as
> *Precondition (human)* on its line; Tophe does it between runs, and the
> planner reports `blocked` while it is missing.
>
> Follow-ups and ideas go to `agent/PROPOSED.md`, never here.

## Fast entry — specs/fast-entry.md

Contract: release `v0.3.0` of `camory/libris-api`, one read-only operation,
`GET /api/v1/isbn/{isbn}`. T020 moves the backend pin to it and T016 the
frontend one; no other task touches the contract (D04).

- [x] T013 Backend: the ISBN-13 value type and the Open Library source.
      `domain`: the ISBN-13 value type — thirteen digits, check digit
      verified, refusing anything else — and the source port, which answers
      what one source knows about an ISBN, or nothing at all: title, subtitle,
      authors with their roles, series and volume number, collection,
      publisher, publication year, language, page count, summary, cover URL
      (D02, D11, PRD §3).
      `infra.lookup`: Open Library, base URL `LIBRIS_OPEN_LIBRARY_URL`, the
      edition followed from `/isbn/<isbn>.json`, its authors fetched by key
      with the role `WRITER`, the cover URL built from the ISBN and never
      fetched, the call bounded by `LIBRIS_SOURCE_TIMEOUT`; an unknown ISBN is
      nothing known, a failure or a timeout is a failure, and the caller tells
      the two apart.
      Tested against WireMock over the recorded answers under
      `src/test/resources/scenarios/open-library`; the whole application still
      boots with neither variable set.
      No use case, no controller, no contract edit; un-skips nothing.

- [x] T020 Backend: the ISBN endpoint and its four answers.
      `application`: the lookup use case over the source port of T013, asking
      Open Library and answering what it knows, that no source knows the ISBN,
      or that no source replied. `infra.web`: `GET /api/v1/isbn/{isbn}`, its
      response and its problems (D02, D11) — not-found when no source knows
      the ISBN, sources-unavailable when every source fails or answers past
      `LIBRIS_SOURCE_TIMEOUT`, validation `{field: isbn, code: not-an-isbn}`
      on a wrong check digit.
      Bumps the backend pin to `v0.3.0` in `ApiContractTest`, the only
      contract edit; Contracteer green on the four responses, the 200 over
      the stubbed use case (D04, D07).
      Realises S4, S7 and the API's own check in S3; un-skips
      `S4 Unknown ISBN` and `S7 Every source down`.

- [x] T014 Backend: the merge rule and several sources.
      The domain rule of S5, tested field by field: the first source's value
      wins for every field it gives, the next fills the fields it leaves
      empty, the cover stays Open Library's by ISBN, and `sources` lists the
      ones that answered (D02).
      The use case asks every source in turn and keeps what the ones that
      replied know; it never sees time, a late source being a failed one by
      the port's contract (D02); it still answers not-found when none knows
      and sources-unavailable when none replies.
      Tested over two fakes: both answering, one failing, both failing. No
      new source and no contract edit.
      Realises the rule S5 and S6 assert; un-skips nothing, both go green
      with T015.

- [x] T015 Backend: the BnF source.
      `infra.lookup`: the SRU client on `LIBRIS_BNF_URL`, queried
      `bib.isbn all "<isbn>"`, ranked before Open Library in the merge; an
      empty record set means it knows nothing (D02).
      The UNIMARC record mapped: title, subtitle, authors with the role their
      function code names and `WRITER` when it names none (PRD §3), series
      and volume number, collection, publisher, publication year, page count,
      and the language as its two-letter code.
      Tested over the recorded answers under
      `src/test/resources/scenarios/bnf`, the full record and the one without
      pages and year.
      Realises S1 (backend), S5, S6; un-skips `S1 Typed ISBN, found`,
      `S5 Merged answer`, `S6 One source down` and
      `S6 One source down, past the timeout`.

- [x] T016 Frontend: the ISBN rule and the lookup client.
      `domain`: the rule of S3 — separators dropped, the old ten converted,
      the check digit verified — answering the ISBN-13 or a refusal, with its
      unit tests (D05).
      `application`: the `IsbnApi` port and its injection key; `infra/api`:
      the hand-written response and problem types and the fetch client,
      sending `Accept: application/json, application/problem+json`, which is
      what makes the mock serve a problem (D05, D06).
      One spec per response the contract declares — 200, 400, 404, 503 — and
      none it does not, against `contracteer mock` (D07).
      Bumps the frontend pin to `v0.3.0` in `vitest.global-setup.ts`, the
      only contract edit (D04).
      `FetchMeApi` sends the same `Accept` header from then on (D06).
      Realises the rule of S3 and the client half of S1, S4, S7; un-skips
      nothing.

- [x] T017 Frontend: the lookup screen, typed ISBN.
      The route `/isbn` and its view: the field labelled `ISBN`, the
      `Chercher` button, the rule applied before anything leaves, the title
      of the answer shown, and the three messages of the scenarios taken from
      the `fr` catalogue (D05).
      A refused text names it and sends no request; an unknown ISBN and
      unavailable sources each say so, switching on the problem's `type`
      (D11).
      The fake of the port lands in `src/fixture`; the view test provides it
      through the injection key (D07).
      The home page links to `/isbn`, so the reader reaches the screen from
      the installed app.
      Realises S3, S4 and S7 on the frontend; un-skips
      `S3 Not an ISBN, wrong length`, `S3 Not an ISBN, wrong check digit`,
      `S4 Unknown ISBN` and `S7 Every source down`.

- [x] T018 Frontend: the card of the answer.
      A presentational component showing what the sources know: subtitle,
      authors with the French word of their role, série and tome, collection,
      publisher, year, the language in French, page count, the ISBN-13, the
      summary, the cover image, and the sources as `BnF` and `Open Library`
      (D05, PRD §3).
      A field the sources did not give shows nothing at all.
      Component tests over its props, the view shows it on a found ISBN.
      Realises S1 on the frontend; un-skips
      `S1 Typed ISBN, found, with hyphens` and
      `S1 Typed ISBN, found, the old ten`.

- [x] T019 Frontend: the barcode scan.
      On `/isbn`, when `BarcodeDetector` announces `ean_13` and the reader
      allows the camera, the camera opens and the first EAN-13 starting with
      978 or 979 runs the lookup of T017 (D05).
      Where `BarcodeDetector` is absent the screen offers the text field
      alone, and the camera stops when the screen goes away.
      Realises S2; un-skips `S2 Scanned barcode`.

- [x] T021 Backend: the BnF alone, with its cover.
      Open Library leaves: `OpenLibrarySource`, `LIBRIS_OPEN_LIBRARY_URL`,
      the merge rule and their tests go; the use case asks the one source
      and `sources` lists `BNF` (D02). `OPEN_LIBRARY` stays in the contract,
      unused: no contract edit.
      The BnF source asks with the thirteen digits and, when they start
      with 978, with the ten as well, since a record made before 2007 holds
      the ten alone (9782253098058 answers to 2253098051 only).
      The BnF source fills `coverUrl` for every record it maps, the
      catalogue's cover URL built from field 003, the ark from `ark:/` on,
      never checked; the card shows its stand-in when the picture is not
      there (T022).
      Tested over the recorded BnF answers, the cover URL asserted on both;
      the S5 and S6 scenario tests go with the rule, the past-the-timeout
      case moves under S7.
      Realises the cover of S1 and the one-source S4 and S7; un-skips
      nothing.

- [x] T022 Frontend: the stand-in of the cover.
      In `SourceEditionCard`, the cover block holds an outlined book icon,
      `ui/components/icons/IconBook.vue`, when `coverUrl` is null and when the
      image fails to load (U05, U06); the picture, when it loads, as before.
      Component tests over the three cases: a cover that loads, none, one
      that errors. No scenario un-skipped, S1's two already pass.
      Realises the stand-in of the Found state.

- [ ] T023 Both sides: the ISBN, one class for both writings.
      The PRD calls it an ISBN: `Isbn13` becomes `Isbn` on both sides, the
      value being the thirteen digits, and holds every rule of the
      identifier: made from either writing, the thirteen digits or the old
      ten ending in a digit or X, separators dropped, each writing checked
      with its own rule; the thirteen digits exposed; the ten, for 978
      alone, derived with its own check digit. The frontend's `isbn13Of`
      moves into it.
      The API admits thirteen digits alone, as the contract's pattern
      says: the controller keeps that check and answers `400_NOT_AN_ISBN`
      to a ten; no contract edit.
      Unit tests on both sides over the writings and the refusals, the
      existing ones kept; nothing else changes behaviour.
      Realises the rule of S3 again; un-skips nothing.

- [ ] T024 Backend: Open Library back, in two requests.
      The merge rule and the use case over several sources return from
      the history of T014 and T021: the first source's value wins for
      every field it gives, the next fills what it leaves empty, `sources`
      lists the ones that know; not-found when a source replied and none
      knows, sources-unavailable when none replied (D02). One change: the
      use case asks every source at once, each on a virtual thread of the
      JDK, and merges in the order of the sources; proven by two fakes
      that each wait to be asked before either answers.
      `OpenLibrarySource` returns reshaped, with `LIBRIS_OPEN_LIBRARY_URL`:
      the edition document (`/isbn/<isbn>.json`, redirect followed) for
      the fields, then one search request
      (`/search.json?isbn=<isbn>&fields=key,author_name,edition_key`) for
      the authors, all writers, from the one work whose `edition_key` holds
      the edition's key, none when no work does; never a request per
      author; the cover by ISBN as before.
      Tested over recorded answers: 9782380751673 (edition and search),
      and 9782253098058 for the pick of the work, whose search answer
      lists two works and only the second holds the edition; the merge
      rule field by field; the use case over fakes as T014 had it. No
      contract edit: `OPEN_LIBRARY` is in `v0.3.0` already.
      Realises S5 and S6, S4 and S7 over two sources; un-skips S5 and S6.

*Done (Tophe, on the Pixel, from the installed app): scan a manga and a BD
and read both cards, the source included; type an ISBN-10 by hand and read its
card; type a wrong ISBN and read the message.*

## Done

- Phase 0 — Foundations, T001 to T012, done 2026-09-10 with `v0.1.3` on the
  Pixel: `/api/v1/me` deployed behind Authelia, the PWA installable, the
  expired session handled. Reviewed by Tophe on 2026-09-08.
