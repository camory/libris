# Scan beep

**Why:** PRD §4.7. A hidden gem: a reader told the trick turns on a
supermarket checkout's beep, and from then on their phone beeps each time
the camera reads an ISBN. Now, for the fun of it, for one reader of the
family, with no name in the code.
**Status:** draft

## Scenarios

The trick is three taps in a row on the title *Ajouter un ouvrage* of the
lookup screen, each less than half a second after the one before. It turns
the beep on, and the beep sounds once as the answer; the same trick turns
it off again, in silence. Nothing on screen hints at it, and the title
looks and reads as before.

The beep is off until the trick turns it on, and stays as the trick left
it on that phone, across closing and reopening Libris; another phone, or
the app's data cleared, starts off again. Nothing is sent to Libris.

While it is on, the beep is the camera's: it sounds the instant the camera
reads an ISBN, before Libris answers, so an ISBN no source knows beeps
too. Nothing else in Libris beeps.

The sound is made by the browser, with no audio file: a steady 1000 Hz
tone for 99 ms at a flat level, with its odd partials, 3000 Hz at −19 dB
and 5000 Hz at −28 dB, the *Imitation* of the test page
(https://claude.ai/artifact/19UpKRcTmYDx7ZfiQ5U4Fv), measured on *Bip de
caisse #2* of La Sonothèque (CC0). Its loudness is the phone's media
volume. A phone lets a page start a sound only from a tap, and the
camera's read is none: the tap that opens the camera readies the sound.

**S1 The trick turns the beep on** · frontend

```gherkin
Given the beep off

When the reader taps the title three times in a row

Then a beep sounds
	And the camera's next read beeps
```

Proof: frontend scenario test over the fakes, the beeps heard counted.

**S2 Three slow taps change nothing** · frontend

```gherkin
Given the beep off

When the reader taps the title three times, waiting more than half a second between two taps

Then no beep sounds
	And the camera's next read is silent
```

Proof: frontend scenario test over the fakes, with the clock under the
test's control.

**S3 The beep off, the camera stays silent** · frontend

```gherkin
Given a phone where the trick was never done

When the camera reads an ISBN

Then no beep sounds
	And Libris looks the ISBN up, as before
```

Proof: frontend scenario test over the fakes.

**S4 The beep on, the camera beeps** · frontend

```gherkin
Given the beep on

When the camera reads an ISBN

Then a beep sounds before Libris answers
	And Libris looks the ISBN up, as before
```

Proof: frontend scenario test over the fakes, the beep heard while
Libris has not answered yet.

**S5 The trick again turns the beep off** · frontend

```gherkin
Given the beep on

When the reader taps the title three times in a row

Then no beep sounds
	And the camera's next read is silent
```

Proof: frontend scenario test over the fakes.

**S6 The beep stays on when Libris is opened again** · frontend

```gherkin
Given the beep turned on by the trick

When the reader opens Libris again
	And the camera reads an ISBN

Then a beep sounds
```

Proof: frontend scenario test over the fakes, the app mounted a second
time over the same phone storage.

**S7 Only the camera's read beeps** · frontend

```gherkin
Given the beep on

When the reader types an ISBN, or closes the camera before it reads one

Then no beep sounds
```

Two cases: an ISBN typed and sent with *Chercher*; the camera opened and
closed with the cross. Proof: frontend scenario test over the fakes, one
test per case.

## Screen

Nothing on screen changes.

## Contract

None: the beep is the frontend's alone.

## Done

On the Pixel, from the installed app on staging, media volume up: scan
*One Piece* 1, no beep. Tap the title *Ajouter un ouvrage* three times
quickly and hear the beep. Scan *One Piece* 1 and hear the beep as the
camera closes, before the card shows; scan an ISBN no source knows and
hear it too. Open the camera and close it with the cross: no beep. Type
9782723488525 and tap *Chercher*: no beep. Close Libris, reopen it, scan
again: the beep. Tap the title three times quickly: no sound; scan: no
beep.

## Tasks

- T138 — S1, S3, S4, S7, the beep's browser adapter — frontend
- T139 — S2 — frontend
- T140 — S5 — frontend
- T141 — S6 — frontend
