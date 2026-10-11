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
