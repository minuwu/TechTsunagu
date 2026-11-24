# :core:design
Owner work package: WP-0C (Design system & shared composables)

This module provides the design system and reusable UI components for the Wakatta Android application:
- Theme & Color Schemes: `WakattaTheme`, `LightColorScheme`, `DarkColorScheme` based on prototype design tokens (`#1f63b5` primary, `--bg`, `--ok`, `--warn`, `--bad`, `--wait`).
- Typography: Material 3 typography with weights 400 and 700 only, body text minimum 16sp, line height 1.7 for Japanese.
- Shapes: Corner radii matching prototype tokens (8dp, 12dp, 16dp, 28dp).
- Status Colors: `StatusColors` providing palette tokens for Wakatta status states (GREEN, ORANGE, RED, WAIT/UNKNOWN).
- Shared Composables: `WakattaTopBar`, `WakattaCard`, `WakattaListItem`, `WakattaChip`, `StatusChip`, `Avatar`, `EmptyState`, `LoadingState`, `ErrorState`, `TriText`.

Note: Custom Bengali font subset bundling is deferred to later packages; system fonts are used by default.
