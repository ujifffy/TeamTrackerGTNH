# Changelog

## 0.3.2

- Keep TeamTracker's own skin-head renderer active for loaded and remote minimap
  players. This removes the broken-looking legacy Xaero Entity Radar handoff at
  vanilla render distance.
- Increased the default minimap head from 15 px to 20 px. Schema 3 upgrades the
  previous default while preserving custom scale values.
- Hide POV heads completely through 16 blocks, then smooth-fade them to the
  configured opacity from 16 to 32 blocks.
- Disabled POV name labels by default and added a one-time schema migration for
  existing 0.3.1 configs. `showInWorldName=true` can still restore them.
- Revalidated the skin UVs and explicitly preserve blended base-face then outer
  head-layer rendering for hair, hats and glasses.
- Added config schema 3. Unknown keys and non-default scale/fade values remain
  preserved; network and server state formats are unchanged from 0.3.0/0.3.1.

## 0.3.1

- Increased the default minimap teammate head from 10 px to 15 px.
- Increased the default fullscreen World Map head from 12 px to 30 px.
- Added independent `minimapIconScale` and `worldMapIconScale` multipliers while
  retaining the existing common `onMapIconScale` value.
- Fixed POV skin overlay visibility: marker opacity is no longer multiplied
  twice, alpha-test clipping is disabled for the marker, and the hat/hair layer
  is explicitly drawn after the base face.
- Added a smooth near-player opacity curve. At point-blank range the marker uses
  35% of `inWorldOpacity` and reaches full configured opacity at 20 blocks.
- Added config schema 2. Existing 0.3.0 values and unknown keys are preserved;
  new visual settings are inserted with their defaults.
- Network and server state formats are unchanged from 0.3.0.

## 0.3.0

- Kept the 0.2.x packet prefix and discriminator stable and added an optional,
  length-prefixed protocol extension for forward-compatible fields.
- Added monotonic full-snapshot sequence numbers and reject delayed, duplicate
  or legacy downgrade packets inside an already sequenced client session.
- Accept legacy 0.2.2/0.2.3 packet layouts and advertise the compatible range
  `[0.2.2,0.4.0)` to Forge/FML. Because 0.2.x itself used FML's exact-version
  default, the first 0.2.x → 0.3.0 deployment still updates both sides together;
  future compatible 0.3.x releases can use the range for rolling replacement.
- Reject malformed, truncated and oversized snapshots without applying partial
  teammate state.
- Added config schema version 1. All 0.2.x keys and values remain unchanged and
  unknown properties are preserved for upgrade/downgrade safety.
- Documented that the mod has no world/NBT registry data to migrate and must be
  replaced only while the Forge server/client process is stopped.
- Re-audited target parity and documented the remaining legacy-Xaero render
  differences separately from gameplay and upgrade safety.

## 0.2.3

- Reject non-finite teammate coordinates on both the server and client.
- Isolate delayed client packet tasks by connection generation so data from an
  old server cannot reappear after disconnect or a rapid server switch.
- Expire teammate snapshots after ten seconds without a complete server update,
  preventing permanent ghost markers when synchronization stops.
- Require an active client world and player before applying a queued snapshot.

## 0.2.2

- Removed custom `ButtonManager.activate`, `deactivate` and `toggle` overrides for Navigator ABI compatibility.
- Navigator's normal layer state, its toolbar button and the map hotkey now stay synchronized.
- Map/world hotkey changes persist to `config/teamtrackergtnh.cfg`.
- In-world markers respect Minecraft's F1 / Hide GUI state.
- Server sync snapshots party membership once per interval instead of repeating team lookups for every receiver pair.
- Corrected the ServerUtilities target to 2.4.9, the version actually shipped by the official GTNH 2.9.0-beta-3 client pack.
- Added `ServerUtilitiesAPI.arePlayersInSameTeam` as the final authorization check before transmitting coordinates.
- Fullscreen world-map markers now include locally loaded teammates instead of assuming Xaero's ordinary entity radar owns them.
- Synchronized the Gradle project and runtime version at 0.2.2.

## 0.2.1

- Fixed the 1.7.10 `RenderWorldLastEvent` camera transform for in-world teammate heads.
- Fixed the test-build OpenGL constants used by the POV renderer.
- POV rendering now uses `renderViewEntity`, restores GL state with `try/finally`, enables textures explicitly and disables culling/depth correctly.
- Cleans cached player heads on reconnect/disconnect and prunes departed teammates.
- Rejects dead/stale live entities during the live/remote handoff.
- Remote interpolation uses the observed packet interval rather than assuming matching client/server config.
- Large same-dimension teleports snap instead of smearing across the map.
- Default in-world minimum distance is now 0 blocks, closer to modern Xaero tracked-player behaviour.

## 0.2.0

- Modern-Xaero-style live/remote handoff.
- Minimap edge clamping, remote interpolation, map/world toggles and scales.
