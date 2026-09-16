# Update

**Why:** PRD §4.8. A reader who installed Libris on their phone gets the
last deployed version in one tap, at a moment of their choosing, instead of
relaunching the app until it happens. Now, because every merge is a deploy
and the family will see the versions go by.
**Status:** planned

## Scenarios

**S1 A new version is ready** · frontend

```gherkin
Given the app open on a version

When a newer version is deployed
     and the app checks

Then one row says that a new version is ready, with the action to update

And the screen keeps what it shows, nothing reloads
```

Proof: frontend scenario test with a stubbed service worker registration
(the row appears on the registration's "waiting" signal).

**S2 The reader updates** · frontend

```gherkin
Given the row of S1

When the reader taps the action

Then the app reloads once
     and runs the new version

And the row is gone
```

Proof: frontend scenario test with a stubbed registration and a stubbed
reload (the waiting worker is told to take over, then one reload); checked by
hand on the Pixel after a deploy of staging.

**S3 The app checks while it stays open** · frontend

```gherkin
Given the app installed and kept open

When it comes back to the foreground
     or an hour passed since the last check

Then it asks the server whether a newer version exists
```

Proof: unit test of the check schedule with fake timers and a stubbed
registration.

**S4 Nothing new** · frontend

```gherkin
Given the app open on the last deployed version

When it checks

Then nothing shows
```

Proof: frontend scenario test with a stubbed registration.

## Screen

Mockup: <https://claude.ai/artifact/NigsrfVApjWpLAVvENrrVo>, one artboard per
state below, drawn with Tophe on 2026-09-16; not exported, the text here is
what the implementer builds from. No route of its own: the banner of U09
above the header of every screen, owned by the app shell.

Top to bottom: the banner, a refresh icon, *Nouvelle version disponible* and
the text button *Mettre à jour* at the right; then the screen as it is.

States:

- **Ready** (S1): the banner.
- **Updating** (S2): the banner reads *Mise à jour…* with a spinner in place
  of the icon and no button, until the reload.
- **Current** (S4): no banner.

## Contract

Nothing: the feature is between the app and its own static server.

## Done

Deploy a version on staging, open the installed app on the phone, bring it
to the foreground: the row shows; tap it: the new version, once.

## Tasks

- T028 — the registration S1, S2 and S3 stand on, no scenario of its own
  — frontend
- T029 — S1, S2, S4 — frontend
- T030 — S3 — frontend
