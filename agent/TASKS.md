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

## Screen — docs/DESIGN.md

One task outside any phase, the bar every screen is reached from.

- [x] T030 Frontend: the tab bar.
      `ui/components`: the tab bar of U03 — on `surface` with a `border`
      hairline above, two tabs of equal width, *Accueil* with a house icon
      to `/` and *Ajouter* with a plus icon to `/isbn`, each its icon over
      its label, 56 tall, `accent` on the screen shown and `muted`
      otherwise, the bottom safe area under it; every word from the `fr`
      catalogue (U04, U07, U08), the two icons beside `IconBarcode`.
      `App.vue` shows it under the footer on every screen, the active tab
      read from the route; the home page keeps its link to `/isbn`.
      The same task draws `HomeView` to U02 and U03, its title and padding
      on the scale's steps, and gives the lookup screen a full-height column
      so the camera view fills the space between the *Chercher* button and
      the tab bar, in place of the fixed `aspect-[3/4]` ratio.
      Component test over the active tab per route; the lookup screen's
      tests unchanged.
      Realises the *Screen* section of the spec, the bar the lookup screen
      is reached from; un-skips nothing.

## Update — specs/update.md

Contract: none, the feature is between the app and its own static server; no
pin moves and no task of the phase touches `camory/libris-api` (D04).

- [x] T028 Frontend: the new version and its banner.
      Precondition (human): `frontend/src/scenario/UpdateScenarios.spec.ts`,
      one skipped test per scenario it realises, bearing the scenario's exact
      title, over a stubbed registration (D07).
      `application`: the `AppUpdate` port and its injection key, which
      announces that a newer version is waiting and takes the reader's order
      to install it (D05).
      `infra/pwa`: the adapter over `navigator.serviceWorker` — it registers
      the worker, announces the one already waiting when the app starts and
      the one that becomes waiting while it runs, tells the waiting worker to
      take over on the order and reloads the app once when the new worker
      takes control; nothing at all where the browser has no service worker.
      `vite.config.ts` turns the plugin from `autoUpdate` to `prompt` and
      injects no registration script of its own, so a new worker waits for
      the reader instead of taking over on the next load; `bootstrap` builds
      the adapter over the real registration and hands it the reload, as it
      hands `FetchMeApi` the redirect, and `createLibrisApp` provides it,
      `main.ts` still the only module reading `import.meta.env` (D05).
      The adapter's spec stubs `navigator.serviceWorker` the way
      `CameraBarcodeScanner`'s stubs `navigator.mediaDevices` and passes a
      fake reload; jsdom seals `window.location` (D07).
      `ui/components`: the banner of U09 — a refresh icon, the sentence
      *Nouvelle version disponible* and the text button *Mettre à jour* at
      the right, every word from the `fr` catalogue (U04, U07, U08); the
      shell shows it above the header of whatever screen is on, and shows
      nothing while no version waits. The tap gives the order: the banner
      reads *Mise à jour…* with a spinner in place of the icon and no button,
      until the reload. Nothing reloads before the tap and the screen keeps
      what it shows. Component tests over the three states of the banner.
      Realises S1, S2 and S4; un-skips `S1 A new version is ready`,
      `S2 The reader updates` and `S4 Nothing new`.

- [x] T029 Frontend: the check while the app stays open.
      The adapter asks the server for a newer worker an hour after its last
      check and whenever the app comes back to the foreground, a foreground
      check starting the hour again; it asks nothing while the app is hidden
      and stops asking when it goes away.
      A unit test with fake timers and a stubbed registration: the hour, the
      return to the foreground, the hour restarted by it, and no check in
      between (D07).
      Neither the banner, the port, the words nor the contract change.
      Realises S3; un-skips nothing, S3's proof being that unit test.

*Done (Tophe, on the Pixel, from the installed app): deploy a version, bring
the app back to the foreground and read the row; tap it and get the new
version, once.*

## Done

- Phase 0 — Foundations, T001 to T012, done 2026-09-10 with `v0.1.3` on the
  Pixel: `/api/v1/me` deployed behind Authelia, the PWA installable, the
  expired session handled. Reviewed by Tophe on 2026-09-08.
- Phase 1 — Fast entry, T013 to T027, done 2026-09-16 with the build of T027
  (`68154ac`) on the Pixel: a manga and a BD scanned and both cards read, an
  ISBN-10 typed, a wrong ISBN's message, 9782380751673 filled by Open Library
  alone.

## Questions for the human

- **Staging.** S2's proof and the spec's *Done* ask for a deploy of staging,
  while D09 knows one environment, the Kimsufi box. No task depends on the
  answer; the hand check is Tophe's step either way.
- **No spec yet**, so nothing is planned for them: PRD §4.1 catalogue, §4.2
  search, §4.3 bookshelves and copies, §4.4 reading, §4.5 series tracking,
  §4.6 wishlist, §4.9 import and export, §4.10 administration, the offline
  browsing of §4.8, and the second spec §4.7 announces, adding the ouvrage of
  the card to a bookshelf.
