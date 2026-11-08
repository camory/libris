# Libris — Progress log, Scan beep

The entries of `agent/PROGRESS.md` of the scan beep, T138 to T141, done
2026-11-08, moved here on 2026-11-08 once what a run needs of them was in
`agent/gotchas/`. Nothing is appended here.

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

## 2026-11-08 — T141 the beep stays as the trick left it on that phone — done
- Did: the beep's state goes through the port `BeepSetting`, kept by
  `WebStorageBeepSetting` as the entry `beep` of the `Storage` it is given;
  `useBeepAtEachRead` starts from it and the trick keeps what it leaves.
  S6 runs un-skipped, over the real `localStorage`, across two openings.
- Decided: nothing the brief left open; `keep` is called at every trick,
  on or off, right after the toggle, before the sound.
- Deviations from the brief: none.
- Left over: the spec's *Done* to Tophe on the Pixel: close Libris,
  reopen it, scan: the beep; the trick again, close, reopen, scan: no
  beep.
