# Public AI nail atelier

- Scope: `AI.html`, public storefront AI creation route
- Mode: Operate
- Audience: design-conscious nail customers and creators using the public storefront
- Primary job: turn a written idea, with an optional reference image, into a usable nail design proposal
- Primary action: generate a nail design
- Proof: horizontal local design archive, task detail, generated result gallery, and iteration entry
- Constraints: retain the homepage navigation in its transparent page-top state and existing scrolled treatment; share prompt compilation, model call, storage, and task APIs with the admin AI; keep public and admin presentation separate; remain a static public frontend
- Direction: a Xiang-embroidery long-scroll atelier using cool porcelain, ink black, lacquer red, translucent paper, brush headings, and real nail imagery
- Signature composition: a horizontal recent-design scroll above a full-width lined creative brief sheet, with craft settings condensed into a segmented modal popover beside the single lacquer-red generation action
- Reference behavior: one unified creation flow serves both modes; no reference means text-to-image, adding one reference switches to image-to-image, and removing it returns to text-to-image without discarding the written brief
- Craft popover accessibility: expose modal semantics, keep focus inside while open, support Escape and backdrop dismissal, reflect trigger and segmented-control state with ARIA, and restore focus to the trigger on close
- Craft selectors: keep each native `select` visually hidden as the form and payload state source; present a custom trigger and listbox synchronized in both directions, use lacquer red only for the selected option, support Escape plus directional-key navigation, collapse on outside click, and stack each trigger/listbox into a single column on mobile
- Workflow: idea -> optional reference -> trend direction -> craft parameters -> generate -> archive/detail -> iterate
- Responsive intent: let the craft modal become a bottom sheet on narrow screens and convert archive/cards/controls to a one-column reading order without changing the workflow
- Verification: desktop browser at 1280 x 720, live record detail retrieval, return-to-create behavior, no horizontal overflow
