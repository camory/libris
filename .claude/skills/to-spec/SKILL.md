---
name: to-spec
description: Turn the current design conversation into a feature spec under specs/, in the house format, with no interview. Interactive sessions only, never a headless run.
disable-model-invocation: true
---

# To spec

Synthesise what the conversation has already decided into
`specs/<feature>.md`. Do not interview Tophe: a decision still open is
named as open in the draft, not asked again and not guessed.

## Process

1. Read what the spec rests on, if not already read in this session:
   the PRD section the feature comes from and the words of `docs/PRD.md`
   §3, `docs/ARCHITECTURE.md`, `docs/DESIGN.md` for a feature with a screen,
   `specs/TEMPLATE.md`, and one spec already done under `specs/` as prior
   art for the tone and the proof lines.

2. Sketch the proofs before the scenarios: for each behaviour, the highest
   seam that proves it, in the words of the template's proof line. Existing
   seams first: a contract example verified by Contracteer on both sides, a
   backend scenario test over HTTP with stubbed sources, a frontend scenario
   test over the fakes, a unit test of one rule, a check by hand on the
   Pixel. The fewer seams the better. Show the list to Tophe and wait for
   his answer before writing.

3. Write the spec from `specs/TEMPLATE.md`, every section, in its order:
   - **Why**: the PRD section, what a reader can do once this exists, why
     now.
   - **Status**: draft.
   - **Scenarios**: stable IDs, Gherkin, the template's layout, words that
     hold on both sides, the proof line of step 2. A scenario's title is
     also its test's name.
   - **Screen**: the route, the layout top to bottom, the French words on
     screen, one bullet per state. The mockups are drawn with Tophe, so the
     section names the states still to draw instead of inventing pictures.
   - **Contract**: the delta the contract gains, one line per operation.
     The contract itself is written and released with Tophe in the same
     session, never by this skill.
   - **Done**: the check Tophe makes on a release, from the phone.
   - **Tasks**: left for the planner.

   Use the words of PRD §3 throughout. No file paths and no code in the
   scenarios; a shape that a snippet states more precisely than prose
   (a schema, a type) may sit in *Contract*.

4. Stop. Tophe reviews the draft; the PR, the mockups, the scenario tests
   committed skipped and the contract release each need his go.

## Out of scope

Tasks, briefs, `agent/TASKS.md`, the contract repository and any code.
