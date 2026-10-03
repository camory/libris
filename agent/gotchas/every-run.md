# Libris — Gotchas, every run

What a run must know before it starts, learned on this tree and still true.
This file is read by every role; `backend.md` and `frontend.md` beside it
by a run that changes that side. Each is read whole, with the Read tool. An
item is a fact that costs a cycle when unknown: a name, a command, a tool's
behaviour, a trap. It goes in the file of the side it concerns, here when
every run needs it. A rule belongs in `docs/ARCHITECTURE.md` or
`docs/DESIGN.md`, a follow-up in `agent/PROPOSED.md`, the story of a task in
`agent/PROGRESS.md`. An item is rewritten or removed the day it stops being
true; the diary keeps the date it was found.

## The sandbox and the tools
- The proof hook counts any command containing `gradlew … check` or
  `npm test` as a gate run, `--dry-run`, pipes and heredoc text included:
  run the gate plainly, last in its command, and write files with the Write
  tool.
- A server started inside a tool call dies with the call (`bootRun`, `npm
  run dev`, `contracteer mock`). Start it as a background task, wait with
  `curl --retry 30 --retry-connrefused`, stop it by task id. A foreground
  `sleep` is refused.
- `pkill -f` or `pgrep -af` with a pattern that appears in the tool call's
  own command line kills or matches the call itself (exit 144). Match on
  something else, or stop the process from what started it.
- `git checkout <file>` reverts to the last commit, not to the working tree:
  commit the cycle before a mutation check.
- `gh pr edit --body` fails on this repository with a GraphQL error about
  classic Projects; a body is updated with `gh api -X PATCH
  repos/camory/libris/pulls/N -F body=@file`. The label form,
  `gh pr edit N --add-label`, works. `gh` GraphQL calls are rate-limited:
  poll `gh pr checks` every 30 s or more, and merge through REST
  (`gh api -X PUT repos/camory/libris/pulls/N/merge -f
  merge_method=squash`) when `gh pr merge` is throttled.
- The loop takes the agent PostgreSQL down at the end of a run; a host gate
  then fails with connection refused until
  `docker compose --env-file agent/.env -f agent/compose.yaml up -d postgres`.
  `backend/.env` points the host gate at that database, and `FreshSchema`
  wipes it: what was entered by hand through `bootRun` is gone after a gate.
- From the host, the frontend gate needs Node 24 and `contracteer` on the
  `PATH`; without them it runs inside the sandbox image,
  `docker run --rm -v "$PWD":/work -w /work/frontend libris-agent:local
  'npm test'`, with the network on, since the mock reads the contract from
  GitHub. The sandbox is the other way round: Node 24, `npm` and
  `/usr/local/bin/contracteer` are there and there is no `docker`, so a run
  inside it calls the gate directly.

## Contract and release
- The contract is OpenAPI 3.1.0 since `v0.9.0`, 3.0.3 before: a field that
  may be null says `type: [<type>, "null"]` where 3.0 said `nullable: true`,
  and a reference that may be null is a `oneOf` of it and `type: "null"`, as
  `Edition.series`. A side whose pin is older still reads 3.0.3. On an
  operation without parameters a response example creates no scenario; the
  verifier emits one generated case.
- On an operation with a `400` response and a typed body, the verifier adds
  a case of its own, `auto: body type mismatch`, and expects `400` with the
  declared problem body: the backend answers a `Problem` to a body of the
  wrong types, not Spring's plain 400. A `format: uuid` path parameter gets
  such a case too, `auto: path 'id' type mismatch`, whose value is
  `<<not-a-uuid>>` since Contracteer 4.1.0 (4.0.0 sent
  `<<not a string/uuid>>`, whose encoded slash Tomcat rejected with its own
  `text/html` 400 before Spring), answered `400`, `404` or `422` with the
  declared problem body. No hand-written `400` scenario removes a generated
  case. The add's bookshelf id is `format: uuid` since `v0.9.0`, so its
  `auto: path 'id' type mismatch` case runs, and `ProblemAdvice`'s
  `TypeMismatchException` handler answers it before the controller.
- A scenario needs its key on a request element when the operation has
  one: a key on the response alone creates no scenario there, and the mock
  answers random data. The request without an optional query parameter is
  the example `null` on that parameter, whose schema must then admit null
  (`type: [<type>, "null"]`); a `null` example on a response header is sent
  as the four letters, so "no header" has no example and a page's end
  travels in the body.
- The verifier checks an answer against the schema only, never against the
  example's value, so an example fixes an input the server must accept and
  nothing else (measured on Contracteer 4.1.1, 2026-09-30). A `2xx`
  response with no key is sent as a generated case, its body built from the
  schema; with a key it runs as that scenario, checked on the schema, so a
  changed example value leaves the run green. A key counts for a response
  when the response holds an example under it or when the key starts with
  its status: `201_ADD_ONE_PIECE_1` on the request alone runs keyed, its
  answer checked on the `Copy` schema, where `ADD_ONE_PIECE_1` would leave
  the `201` generated; `404_UNKNOWN_ISBN` on the request alone builds the
  404 scenario. A keyed error case with no body example still checks the
  status, the content type and the `Problem` schema. A generated `isbn13`
  only matches the pattern and fails its check digit about nine times in
  ten, hence the keyed examples of the ISBN operations' 200 and 201. The
  response examples of the document exist for the mock only (see the mock
  items of `frontend.md`). A response with two content types gets one
  generated case per type, each with a random parameter value; the cover
  operation answers `image/*` alone, one case sent with `Accept: image/*`,
  any image accepted, no example needed. `externalValue` is not read: the
  example is seen as null.
- The backend's `ApiContractTest` is a web slice over `MockitoBean` use
  cases, so a stub answering `any()` serves the generated cases, where a
  stub on exact arguments answers nothing to a random value. The reviewer
  checks the number of cases a PR claims against `tests=` in
  `backend/build/test-results/test/TEST-fr.amory.libris.ApiContractTest.xml`:
  read it there after the gate, never count by hand.
- `additionalProperties: false` cannot sit on a branch of an `allOf`: the
  standard applies each branch on its own, so the base refuses the fields
  the other branch adds, and no instance passes. Contracteer merges the
  branches first and accepts such a document, a divergence its coverage
  page records since 4.1.1; the contract keeps the keyword off `Edition`
  and off the three schemas composed over it, and the verifier checks an
  answer for extra fields only where a schema says the keyword. A property
  declared again in a second branch tightens the base's, `isbn13` never
  null on `IsbnLookup`.
- Contracteer honours `readOnly`: the mock answers `400` to a request whose
  body carries a read-only field, even empty. The contract avoids `readOnly`
  and gives a request its own schema instead.
- Body examples live under `components/examples` and the operations point at
  them with `$ref`; the mock and the verifier resolve them. YAML anchors and
  the `<<` merge key stay out of the document: `<<` is YAML 1.1, and a raw
  reader sees the anchor, not the body.
- A release is `git tag vX.Y.Z <merge sha> && git push origin vX.Y.Z`, then
  `gh release create vX.Y.Z --title vX.Y.Z --generate-notes`; `--target
  <sha>` is refused. Tag only after the CI run on `main` has pushed the
  `sha-` images. The ghcr images are public: pulling needs no login.
- The `images` job proves an image builds, nothing runs it: nginx, the
  `HEALTHCHECK`, the SPA fallback and the `/session` redirect are exercised
  only by a deploy and the phone check.
