# Libris — Design and screen rules

> Read by every frontend run. Rules here are binding until changed by a human,
> like the decisions of `docs/ARCHITECTURE.md`; each has an ID so specs, briefs
> and PRs can reference it. A spec's *Screen* section describes one screen and
> assumes everything below; it repeats nothing from here.
> Written with Tophe on 2026-09-12 from the lookup screen mockups
> (`specs/fast-entry/`).

## Rules

### U01 — Palette: slate, one accent, one danger, dark follows the system
Six colour roles, every one a Tailwind shade, one value per scheme:

| Role      | Light                | Dark                 | Used for                              |
|-----------|----------------------|----------------------|---------------------------------------|
| `bg`      | slate-50 `#f8fafc`   | slate-900 `#0f172b`  | the page                              |
| `surface` | white `#ffffff`      | slate-800 `#1d293d`  | cards, fields, the tab bar            |
| `text`    | slate-900 `#0f172b`  | slate-100 `#f1f5f9`  | everything that is read               |
| `muted`   | slate-500 `#62748e`  | slate-400 `#90a1b9`  | hints, labels, inactive tabs          |
| `border`  | slate-200 `#e2e8f0`  | slate-700 `#314158`  | hairlines, field outlines, skeletons  |
| `danger`  | red-700 `#b91c1c`    | red-400 `#f87171`    | error outlines and messages           |

One accent, `accent`, teal-700 `#0f766e` in both schemes, teal-800 `#115e59`
when pressed: the primary button, the active tab, the scanner icon and
brackets, the series line of a card. White text sits on it. No other hue
appears; a second accent is a decision to make here first.

The scheme follows the system (`prefers-color-scheme`); the app offers no
toggle and stores no preference. Both schemes are the same layout with the
table's values swapped, so a component is designed once.

The roles are the only colour names components know: they say `bg-surface`,
`text-muted`, `border-danger`, never a slate or teal shade. The roles are
declared once as Tailwind theme tokens in `style.css`, each resolving to its
light or dark value there; a change of shade is one line in that file.

### U02 — Type: the system font, one scale
The system font stack (`system-ui, -apple-system, "Segoe UI", sans-serif`),
antialiased; no webfont is loaded, ever. Numbers that line up, an ISBN or a
page count, use tabular figures.

One scale, in pixels, each step with its one job:

| Step         | Size / weight     | Job                                             |
|--------------|-------------------|-------------------------------------------------|
| page title   | 22 / semibold     | the `h1` of a screen, tight letter-spacing      |
| card title   | 20 / bold         | the title of an ouvrage, line-height 1.2        |
| field        | 18 / regular      | what the reader types                           |
| button       | 16 / semibold     | the primary action                              |
| lead         | 15 / regular      | a subtitle, an empty-state sentence             |
| body         | 14 / regular      | hints, messages, label/value rows, line 1.4     |
| label        | 13 / semibold     | a field label, in `muted`; chips, `Sources`     |
| overline     | 12 / semibold     | uppercase, tracked, in `accent`: série · tome   |
| tab          | 12 / medium       | the tab bar; semibold on the active tab         |

A screen uses the steps and nothing between them: no 17, no 21. A step that
no job fits is added here, not improvised in a component. Weight carries the
hierarchy; colour (`text`, `muted`, `accent`) carries the role; size does the
rest.

### U03 — Page: header, content, footer, tab bar
Every screen is one column, designed at 390 px wide and read top to bottom:

1. **Header**: the page title and, under it, one hint sentence in `body`,
   `muted`, ending with a full stop. Padding 20 at the sides, 20 above,
   12 below. Nothing else lives there: no back arrow, no action button.
2. **Content**: the blocks of the screen, 20 apart, 20 of side padding, on
   `bg`. This is the part that scrolls; header, footer and tab bar stay put.
3. **Footer**: the revision, `label` step, `muted`, centred, one line; it
   sits under the content and above the tab bar on every screen.
4. **Tab bar**: on `surface` with a `border` hairline above, one tab per
   top-level screen, equal widths: *Accueil* (a house) and *Ajouter* (a
   plus). A tab is its icon over its label, 56 tall, `accent` when it is the
   screen shown and `muted` otherwise. Below it the padding the phone's
   home indicator needs (the bottom safe area, 16 at least).

Spacing has three sizes: 20 between blocks, 14 inside a card, 8 between a
control and what belongs to it (a label and its field, a field and its
button, an icon and its text). Radii have two: 12 on controls, 14 on cards
and sheets; icons inside a control get 10.

Wider than a phone, the column keeps its width, capped at 480 and centred on
`bg`; nothing rearranges into several columns. Desktop is a bonus (PRD §4),
not a second layout.

### U04 — Controls: field, primary button, icon button
**Field.** Its label above it, `label` step, `muted`; then the box, 50 tall,
full width, `surface`, a 1.5 px `border` outline, radius 12, 14 of side
padding, the text in the `field` step. The placeholder is an example of a
valid value in `muted`, never an instruction. The keyboard matches the value:
numeric for an ISBN. What the reader types stays as typed; the rule that
reads it lives in the domain, not in the field. A field the answer refuses
takes a `danger` outline and keeps its text; the message goes under it (U05).
An icon that acts on the field, the scanner, sits inside the box at the
right, as an icon button.

**Primary button.** One per screen, the action the screen is for. Under the
field it serves, 8 below it, full width, 50 tall, radius 12, `accent` with
white text in the `button` step, `accent` pressed shade on press. Its label
is a verb in the infinitive: *Chercher*. While its action runs it is busy:
the label becomes the action in progress with an ellipsis, *Recherche en
cours…*, a white spinner turns at its left, the button dims and accepts
nothing; the field stays editable. It comes back to itself when the answer
arrives, whatever the answer is.

**Icon button.** An icon alone, 44 by 44, radius 10, no border, no
background, `accent`; always an `aria-label` that names the action in
French, *Scanner le code-barres*, *Fermer la caméra*. An icon button that
toggles a state changes its icon, the barcode becomes a cross, and its label
with it.

Every control is at least 44 tall and wide, reachable one-handed at the
bottom half of the screen: the field, its button and the tab bar are the
lowest things on the page, the title is the highest.

### U05 — Feedback: message, skeleton, empty state
A screen answers under the control that asked, in the place the answer will
take, and one answer replaces the previous: a message replaces a card, a
card replaces a message, never two at once.

**Message.** A round alert icon, 22, and one sentence in the `body` step,
both in `danger`, 8 apart, left-aligned under the field that failed, which
takes the `danger` outline (U04). The sentence is short and says what is
wrong, not what to do, when the fix is obvious: *ISBN invalide*, *ISBN
inconnu*. When the reader can do nothing but wait, it says so and no more:
*Erreur lors de la recherche, veuillez réessayer plus tard.* Colour is never
the only signal: the icon and the words carry it too. There is no success
message; the result itself is the success.

**Skeleton.** While an answer is on its way, a grey silhouette of it stands
where it will appear: the card's outline on `surface`, its cover and text
lines as `border` blocks, rounded 6, at 80 % opacity, no animation. It is
the same size as the thing it announces, so nothing jumps when the answer
lands. It appears with the busy button (U04) and leaves with it.

**Empty state.** A screen with nothing to show yet centres an outlined icon,
40, `muted` at 60 %, over one sentence in the `lead` step, `muted`, 28 of
padding around. The sentence says what will fill the space, not that it is
empty. The lookup screen has none: before the first search, the space under
the button is simply empty.

No toast: an answer stays where it landed until the next one replaces it,
and it never floats over the page. The camera sheet is a block of the
content, under the button, not a layer over the page.

### U06 — Cards and chips
**Card.** A card shows one thing the app knows, an ouvrage: `surface`, a
1 px `border`, radius 14, 16 of padding, its parts 14 apart. Top part, side
by side: the cover at the left, 96 by 149, radius 6, `border` behind it
while it loads or when there is none, never stretched; at its right,
stacked 6 apart, the overline (série · tome, U02), the card title, the
subtitle in `lead` `muted`, and one line per author in `body`, the name in
`text` and its roles after a middle dot in `muted`: *Eiichirō Oda · scénario,
dessin*. Middle part: one row per field, `body`, the label at the left in
`muted` and the value at the right in `text`, right-aligned, 7 of vertical
padding, a `border` hairline above each row: *Collection*, *Éditeur*,
*Année*, *Langue*, *Pages*, *ISBN*, in that order. A field with no value has
no row; a card never says *inconnu*. Then the summary, when there is one,
as a paragraph in `body`, `text`, line-height 1.4, as long as it is. Bottom
part: the word *Sources* in the `label` step, `muted`, followed by one chip
per source.

**Chip.** A word in a pill: `label` step, `text`, a 1 px `border`, radius
999, 4 by 10 of padding, no background. It states, it does not act: a chip
is never a button. Chips wrap on their line, 8 apart.

A card is presentational: props in, nothing out. It shows what it is given
and decides nothing about it; the words it displays for a role or a
language come from the `fr` catalogue, keyed by the code the API answers.

### U07 — Icons: hand-drawn, inline, no library
An icon is an inline SVG on a 24 grid, stroke 1.8, round caps and joins,
`currentColor`, no fill; 22 for the alert of a message, 40 for an empty
state (U05). Each is its own component under `ui/components/icons`, named
by what it shows (`IconHome`, `IconBarcode`, `IconClose`, `IconPlus`,
`IconAlert`, `IconBook`), drawn once and reused. No icon font, no icon
package (D05: no UI library).

An icon next to a word is decoration: `aria-hidden`, the word carries the
meaning. An icon alone is a button (U04) and carries its `aria-label`.

### U08 — Words on screen
French, in the words of PRD §3: ouvrage, série, tome, collection, auteur,
lecteur, bibliothèque, exemplaire, and a role as the activity, not the
person: scénario, dessin, couleurs, traduction. Every sentence and label lives in the
`fr` catalogue; a component never holds a French string.

Sentences are short, in the plain present, without exclamation marks, and
end with a full stop when they are sentences (*Scannez le code-barres ou
saisissez l'ISBN.*) and without one when they are labels or messages of a
few words (*ISBN*, *ISBN invalide*, *Chercher*). The reader is addressed as
*vous*. An action in progress ends with an ellipsis (*Recherche en
cours…*). A title names the thing done on the screen, in the infinitive with
its object: *Ajouter un ouvrage*. Typography is French: the non-breaking
space before `:`, `;` and `?`, guillemets « » for quotes, the middle dot
` · ` to join two facts on one line (*One piece · tome 1*).

Proper names keep their own spelling and case, *Open Library*, *BnF*,
*Eiichirō Oda*; a title of an ouvrage is shown as the source gives it.
