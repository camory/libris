# Update

**Why:** PRD §4.8. A reader who installed Libris on their phone gets the
last deployed version in one tap, at a moment of their choosing, instead of
relaunching the app until it happens. Now, because every merge is a deploy
and the family will see the versions go by.
**Status:** draft

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

Mockup to draw with Tophe. Proposal: the row is a block of the content at
the top of every screen, under the title, *Nouvelle version disponible* at
the left and the action *Mettre à jour* at the right; it is not a layer over
the page and it does not float (U05). No version number: the reader has no
use for a commit hash.

States:

- **Ready** (S1): the row.
- **Updating** (S2): the action reads *Mise à jour…* until the reload.
- **Current** (S4): no row.

## Contract

Nothing: the feature is between the app and its own static server.

## Done

Deploy a version on staging, open the installed app on the phone, bring it
to the foreground: the row shows; tap it: the new version, once.

## Tasks

Filled by the planner.
