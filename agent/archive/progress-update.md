# Libris — Progress log, Update

The entries of `agent/PROGRESS.md` of the update feature, T028 and T029,
done 2026-09-18, moved here on 2026-09-21 once what a run needs of them
was in `agent/GOTCHAS.md`. Nothing is appended here.

---

## 2026-09-18 — T028 The new version and its banner — done
- Did: `application/AppUpdate.ts` declares the port (`onNewVersion`,
  `install`) and its key; `infra/pwa/ServiceWorkerAppUpdate.ts` registers
  `/sw.js`, keeps the worker that reaches `installed` while a controller is
  already running, sends `{ type: "SKIP_WAITING" }` on the order and reloads
  once on `controllerchange`; `ui/components/AppUpdateBanner.vue` draws U09
  from a `state` prop, `App.vue` holds that state and the three words come
  from the catalogue. The plugin moved to `registerType: "prompt"` with
  `injectRegister: false`, `nginx.conf` lost its `/registerSW.js` block, and
  the three Update scenarios are un-skipped and green: thirteen cycles, gate
  green, 90 tests.
- Decided:
  - **The shell owns the banner, so the shell injects the port.** No view
    owns chrome that stands above every screen, and a store for two refs
    would be a layer for nothing. `eslint.config.ts` widens `src/ui/App.vue`
    alone through a `shell` category; the same import from `src/ui/i18n.ts`
    is still refused, which the mutation probe proved. D05 rule 4 says the
    views alone inject the ports: the pull request proposes its amendment.
  - **A worker can be installing before the adapter can listen.** `S2` finds
    the new version on the tick that mounts the app, so `updatefound` fires
    while `register()`'s promise is still pending and the announcement is
    lost. The adapter watches `registration.installing` when the promise
    resolves too; that is the ninth adapter case.
  - **The first install says nothing.** A worker reaching `installed` with no
    controller in charge is the app's first worker, not a newer version. The
    scenarios cannot catch it — their container always has a controller — so
    it has its own case.
- Deviations from the brief: the ninth adapter case above, which the brief's
  eight did not foresee; `FakeAppUpdate` is the port's two methods and no
  more, the `announce()` and the order count the brief sketches having no
  caller now that `createLibrisApp.spec.ts` keeps its two cases.
- Left over: nothing of the task. The update that never finishes is in
  `agent/PROPOSED.md`.
- Fix-ups on review with Tophe: the state type declared once, the failed
  install as a tenth adapter case, `BusySpinner` shared by the busy button and
  the banner, and D05 rule 4 amended for the app shell.

## 2026-09-18 — T029 The check while the app stays open — done
- Did: `infra/pwa/ServiceWorkerAppUpdate.ts` keeps the registration once it
  resolves and holds a clock of its own: `betweenChecks` (one hour) arms a
  `setTimeout` that calls `registration.update()`, catches its refusal and
  arms the next hour; a `visibilitychange` listener on `document` checks and
  restarts the hour when the app is visible, and cancels the pending check
  when it is hidden, so nothing survives the app going away. Eight cases in
  the adapter's spec, over fake timers and an `update` spy on the file's own
  stubbed registration: eight cycles, gate green, 99 tests.
- Decided:
  - **The rejection a spy returns is not an unhandled one.** Vitest's `vi.fn`
    attaches its own handler to the promise it records, so a rejecting
    `update()` left uncaught raises nothing the spec can see: *keeps asking
    when a check fails* is green whether or not the `catch` is there. Its
    proof is the mutation probe instead — a schedule re-armed inside
    `.then()` reds the case at one call *and* makes the run report an
    unhandled error — and the gate's own silence on the committed tree.
  - **The restarted hour is proven from half an hour in.** The foreground
    return written at t=0, as the brief sketched it, passes against an adapter
    that ignores the foreground entirely: the hour armed by the registration
    was already about to fire. The case lets half the hour pass first, so the
    pending check and the restarted one fall an hour apart and only the
    cancel-and-re-arm keeps the count at one.
  - **`check()` arms the next hour whether or not it had a registration to
    ask.** The one `null` guard the brief allows is the optional call;
    splitting the schedule on it would be a branch no case motivates.
- Deviations from the brief: none of substance. The brief counts eight
  existing cases in the adapter's spec and 90 on `main`; the file holds ten,
  the run counted 99 with the eight new ones, so `main` carried 91.
- Left over: nothing of the task. The first foreground check after a long
  Android background may fire before the network is back, and the next answer
  is then an hour away; the pull request says so, and a retry is a spec
  conversation, not a fix inside a run.
- Fix-ups on review with Tophe: a guard in `scheduleCheck()` so an app hidden
  before its registration resolved arms nothing, with its case; the
  no-service-worker case named for what it asserts; the null-registration
  re-arm left as is, the spec's leaked `document` listeners making it
  unobservable (in `agent/PROPOSED.md`).
