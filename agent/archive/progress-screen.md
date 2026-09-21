# Libris — Progress log, Screen

The entries of `agent/PROGRESS.md` of the screen work, T030 and the
revision on the home page, done 2026-09-17, moved here on 2026-09-21 once
what a run needs of them was in `agent/GOTCHAS.md`. Nothing is appended
here.

---

## 2026-09-16 — T030 The tab bar — done
- Did: `AppTabBar.vue` shows a house linking to `/` and, in the middle of the
  bar, a barcode in an `accent` circle linking to `/isbn`, *Accueil* and
  *Ajouter* their `aria-label`s, on `surface` with a hairline above, 48 tall,
  and a bottom padding of `env(safe-area-inset-bottom)`; `App.vue` became the full-height
  shell that scrolls the view and holds the footer and the bar; `HomeView` was
  redrawn to the column and the type scale, and the lookup screen's camera
  block now grows into the space left instead of keeping a ratio. New entries
  `tabs.home` and `tabs.add`.
- Decided:
  - **The bar after BDGest's** (Tophe, on review): the run drew two equal
    tabs, a house and a plus, each its icon over its word. The review settled
    on the shape of BDGest's bar without its labels: the house alone on the
    left, and in the middle an `accent` circle holding the barcode the field
    already uses, *Accueil* and *Ajouter* as `aria-label`s, no plus; three
    columns, the third empty until a screen claims it. `IconPlus` and the
    `tab` step were removed, U02, U03 and U07 and the spec's *Screen* section
    amended in the same pull request.
  - **The inactive look is inherited, the active look is the element's own.**
    `text-muted` sits on the `<nav>` and `text-accent` in
    `exact-active-class` on the anchor, so the active tab wins by the
    cascade rather than by the order Tailwind happens to emit its utilities.
    Both on the same element would make the colour depend on that order.
  - **The bar reads the route through the exactly-active link.** `/` is a
    prefix of every path, so `router-link-active` marks *Accueil* everywhere;
    only `aria-current="page"` and `exact-active-class` follow the screen
    shown. Both route cases were green the moment the two links existed, so a
    mutation check (`to="/isbn"` pointed elsewhere) was run to prove they
    disagree with the code.
  - **The lookup column is `min-h-full`, not `h-full`.** The camera block takes
    the leftover space with `flex-1`, and a long *Found* card still grows past
    the viewport and scrolls.
- Deviations from the brief: none.
- Left over: nothing of the task. The footer keeps the scaffold's look
  (`p-4 text-sm`), which the brief puts out of scope; it is in
  `agent/PROPOSED.md`.

## 2026-09-17 — The footer retired, the revision on the home page — done
- Did: `App.vue` is the scrolling view over the tab bar and nothing else;
  `AppFooter.vue` and its spec are gone. The revision reaches `HomeView`
  through `revisionKey` (`application/Revision.ts`, beside the port keys, the
  only place a view may import from), provided by `createLibrisApp`,
  and ends the home page in the `label` step, `muted`. U03 and D09 say so.
  Written with Tophe on the review of T030, no task line: two cycles, gate
  green.
- Decided: **nothing needs to stay visible under every screen.** The footer
  held only the revision, a support aid for Tophe; the update banner (U09)
  will tell the reader when a newer version exists, so the line moves to the
  foot of the home page and every screen gets its height back.
- Left over: nothing.
