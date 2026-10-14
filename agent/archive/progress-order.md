# Libris — Progress log, Order on the screen

The entries of `agent/PROGRESS.md` of the order on the screen, T086 and
T087, done 2026-10-12, moved here on 2026-10-14 once what a run needs of
them was in `agent/gotchas/`. Nothing is appended here.

---

## 2026-10-11 — T086 The card and a catalogue row sort their authors and bookshelves — done
- Did: `nameOrder(locale)` in `domain`, an `Intl.Collator` ignoring case and
  accents; `authorsInOrder` and `bookshelvesHolding` group and sort through
  it; the card and a catalogue row take both lists from them.
- Decided: the card's one-line test compares `roles.join()` alone, the roles
  arriving in role order from `authorsInOrder`, so the card's own sort of a
  set's codes left. The card's rows iterate `{ bookshelf, count }`, the
  shape the domain answers, keyed on the bookshelf's id as before.
- Deviations from the brief: none.
- Left over: T087 drops the backend's order once this is deployed; the
  examples *Erckmann, Chatrian* of U06 and `specs/kind.md` are proposed in
  the PR to read *Chatrian, Erckmann*, for Tophe to settle.

## 2026-10-11 — T087 Authors and a card's bookshelves in no promised order — done
- Did: `Contributions.of` keeps one contribution per author and role and
  orders nothing; `ContributionRole` is four bare entries; `LookupIsbnForReader`
  answers a card's copies in the repositories' order.
- Decided: `Contributions` holds a `Set` built from the distinct list, so it
  stays equal whatever the order given, as the brief's *Risks* measured;
  the guard on that equality came first, committed before its mutation ran.
- Deviations from the brief: none.
- Left over: the scenario comparisons of `authors` in order, proposed in
  `agent/PROPOSED.md` for Tophe; merging is not deploying, the backend
  drops the order once Tophe deploys it.
