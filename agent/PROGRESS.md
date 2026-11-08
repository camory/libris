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

## 2026-11-08 — T138 the trick turns the beep on at the camera's read — done
- Did: three taps on *Ajouter un ouvrage* turn the beep on and sound it;
  while the lookup screen stays open each camera read beeps before the
  lookup, through `WebAudioBeeper`, the frontend's first sound.
- Decided: `ready()` calls `resume()` whatever the context's state: no test
  motivates reading `state`, and resuming a running context does nothing.
  `ready()` joined the port at step 7, where the adapter first needed it;
  `FakeBeeper` gained `readies` at step 10, where the view first read it.
  One run handed off after step 15 and the refactor; a second finished.
- Deviations from the brief: none.
- Left over: the pace between taps (T139), turning the beep off (T140),
  keeping it across openings (T141); for Tophe on the Pixel, the tone
  against the spec's test page, the beep after reopening the screen from
  the tab, and the title's look under three quick taps.

## 2026-11-08 — T138 the trick turns the beep on at the camera's read — reworked
- Did: `useBeepAtEachRead` exposes `beep()`, its verb, and `tap()`, the
  view calling `beep()` at the camera's read; `WebAudioBeeper.beep()`
  resumes a suspended context before it sounds; its module reads top down.
- Decided: Tophe's answer of 2026-11-08 on #355 amended the brief: the
  composable's shape is `beep()` plus `tap()`, as `find` plus `refresh`,
  D10 unchanged; the cold load on `/isbn` is met by `beep()` resuming a
  suspended context, `tap()` never calling `ready()`. `beep()` reads the
  state before resuming, as his answer words it, proven by a second case
  over a running context; `ready()` still resumes whatever the state.
- Deviations from the brief: none beyond the amended shape.
- Left over: for Tophe on the Pixel, besides the first run's three checks,
  the trick's beep after a reload on the lookup screen.

## 2026-11-08 — T139 three slow taps change nothing — done
- Did: the trick counts only taps in a row, each less than half a second
  after the one before; a slower tap starts a new row of which it is the
  first. `tap(at)` takes the click's `timeStamp`; S2 runs un-skipped.
- Decided: the composable keeps the last tap's instant, starting at
  `-Infinity`, so the first tap of all starts a row with no special case;
  the bound is a module constant, `halfASecond`, compared strictly.
- Deviations from the brief: none.
- Left over: the fourth quick tap and turning the beep off (T140),
  keeping it across openings (T141); for Tophe on the Pixel, three quick
  taps at a natural pace still turn the beep on, three taps a second
  apart sound nothing.

## 2026-11-08 — T140 the trick again turns the beep off, in silence — done
- Did: the third tap of a row turns the beep on and sounds it when it is
  off, and turns it off in silence when it is on; the tap after the trick
  starts a new row however soon it comes. S5 runs un-skipped.
- Decided: the trick toggles `on` and calls the composable's own `beep()`,
  so the sound follows the state it just took; the row restarts by setting
  the count back to 0, the pace rule then counting the next tap as the
  first.
- Deviations from the brief: none.
- Left over: keeping the state across openings (T141); for Tophe on the
  Pixel, three quick taps with the beep on make no sound and the next scan
  is silent, six quick taps sound one beep and leave the scan silent.
