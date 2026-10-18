# TeamTrackerGTNH 0.3.2 — rendering parity review

Version 0.3.2 preserves the server logic and the 0.3.0/0.3.1 wire format. Changes are limited to client rendering and
config schema 3.

## Fixed

| Area | 0.3.1 | 0.3.2 |
| --- | --- | --- |
| Loaded player on the minimap | TeamTracker stopped drawing and handed the point to legacy Xaero Entity Radar | TeamTracker keeps drawing the same head by default; a manual compatibility switch is retained |
| Minimap head size | 15 px | 20 px; separate `minimapIconScale=2.0` |
| Nearby POV marker | Non-zero opacity even at point-blank range | Zero alpha through 16 blocks, smoothstep fade from 16–32, then `inWorldOpacity` |
| POV name | Enabled | `showInWorldName=false` by default |
| Outer head layer | Second UV pass | Second UV pass revalidated; blend and render state are isolated |
| Config | Schema 2 | Schema 3 with a visual-default migration |

The `PlayerHeadIcon` UV layout remains standard for its normalized 64×32 texture: the base face uses
`(0.125, 0.25)–(0.25, 0.5)`, and the outer head layer uses `(0.625, 0.25)–(0.75, 0.5)`. Hair, hats and glasses are
drawn in a second blended pass over the face with the same alpha, so opacity is not applied twice.

## 0.3.1 → 0.3.2 migration

- The previous default `minimapIconScale=1.5` becomes `2.0`; a custom value is preserved.
- `useXaeroRadarForLoadedPlayers` is set to `false` once.
- The previous default `inWorldFullOpacityDistance=20` becomes `32`; a custom value is preserved.
- `inWorldHiddenDistance=16` is added.
- `showInWorldName` is set to `false` once and can be re-enabled manually afterwards.
- Forge Configuration preserves unknown keys.

## Remaining differences from modern Xaero

- Navigator does not expose the minimap shape, so edge clamping remains radial.
- World Map names are always visible instead of using the native hover animation.
- The POV marker remains a camera-facing world billboard rather than a full port of modern Xaero's screen-space and
  crosshair-hover renderer.
- Enabling `useXaeroRadarForLoadedPlayers=true` manually restores the legacy native handoff and its visual differences.
