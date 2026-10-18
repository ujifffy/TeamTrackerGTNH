# Team Tracker GTNH 0.3.2

Backport-style teammate tracking for **GTNH 2.9.0-beta-3**, targeting the behaviour of
**Xaero Tracked Players + FTB Teams** used by GregTech Community Pack Modern.

## Runtime requirements

- Minecraft 1.7.10 / Forge 10.13.4.1614
- ServerUtilities **2.4.9**
- Navigator **1.1.9**
- Xaero's Minimap **21.10.44** and Xaero's World Map **1.14.1.33** are the intended GTNH map versions

Install the same jar on the **server and every client** that should receive teammate positions.

## Current target behaviour

- ServerUtilities party replaces FTB Teams as the relation source.
- Only online members of the same ServerUtilities team are synchronized.
- The public ServerUtilities same-team predicate is the final server-side authorization check before coordinates are sent.
- When a teammate exists as a real client `EntityPlayer`, their live client position is preferred.
- The TeamTracker layer owns both loaded and remote minimap players by default, so the face renderer does not change at vanilla render distance.
- When the teammate leaves the minimap frame, a tracked-player head is pinned to the minimap edge.
- When the teammate leaves vanilla entity tracking range, the server position automatically takes over.
- Remote server snapshots are interpolated instead of jumping every sync packet.
- All current-dimension teammates, including locally loaded players, remain visible on the fullscreen Xaero World Map with their skin head and name.
- In-world/POV markers use the same live-position-first rule, stay hidden through 16 blocks and fade smoothly to full configured opacity by 32 blocks.
- On-map icon scale, in-world icon scale and in-world name scale are configurable in the same spirit as modern Xaero.
- Two unbound Controls keybinds reproduce modern Xaero's separate map/world tracked-player toggles and persist their state.
- The Navigator layer button uses Navigator's normal active/inactive state instead of overriding its lifecycle methods.
- In-world markers respect Minecraft's F1 / Hide GUI setting.

The optional distance text under the in-world marker is intentionally an extra convenience feature; it can be disabled.

## Safe upgrade

1. Stop the dedicated server cleanly. Forge 1.7.10 mods cannot be hot-swapped in a running JVM.
2. Replace the old TeamTrackerGTNH jar with `teamtrackergtnh-0.3.2.jar`; do not keep both versions.
3. Replace the jar in every client instance while Minecraft is closed.
4. Start the server and clients normally. Keep the existing world and `config/teamtrackergtnh.cfg`.

The mod owns no blocks, items, entities, dimensions, saved-world data or player NBT. Version 0.3.2 therefore has no
world migration. Config schema 3 changes only the visual defaults listed below; custom scale/fade values and unknown
keys are preserved.

The network discriminator and the complete 0.2.x packet prefix stay unchanged. 0.3.x adds only an optional
length-prefixed extension and accepts remote wire versions in `[0.2.2,0.4.0)`. However, 0.2.x itself used FML's
default exact-version check, so a mixed 0.2.x/0.3.x connection is not guaranteed to pass the old side's handshake.
Perform the first upgrade from 0.2.x on the server and every client in one maintenance restart. The explicit range makes
future compatible 0.3.x rolling upgrades possible from this release onward.

0.3.2 changes only client rendering and config schema metadata. Its wire format is identical to 0.3.0/0.3.1, so
0.3.x servers and clients can coexist temporarily while the jars are replaced. Replace every copy before judging
visual behaviour because all map and POV fixes execute on the client.

Schema 3 performs a one-time visual-default migration: it disables the legacy loaded-player handoff, changes the old
default minimap scale from 1.5 to 2.0, changes the old default full-opacity distance from 20 to 32, adds a 16-block
hidden distance and disables the POV name. Custom minimap scales and fade distances that differ from the old defaults
are preserved, as are unknown keys. The name can be re-enabled after this migration if desired.

## Important Xaero setting

Keep `useXaeroRadarForLoadedPlayers=false` (the default). Legacy Xaero's native player radar can render a different or
broken-looking marker exactly when a player enters vanilla tracking range. TeamTracker now uses the same two-layer
skin head on both sides of that boundary. The compatibility switch remains available for setups that deliberately want
the old native-radar handoff.

## Config

Generated at `config/teamtrackergtnh.cfg`.

Most relevant options:

```text
client.map {
    showMapHeads=true
    clampOffscreenMapHeads=true
    useXaeroRadarForLoadedPlayers=false
    interpolateRemotePlayers=true
    onMapIconScale=1.0
    minimapIconScale=2.0
    worldMapIconScale=2.5
    showWorldMapNames=true
    worldMapNameScale=0.75
}

client.world {
    showInWorldHeads=true
    showInWorldName=false
    showInWorldDistance=false
    inWorldOpacity=0.45
    inWorldHiddenDistance=16
    inWorldFullOpacityDistance=32
    inWorldMinDistance=0
    inWorldMarkerDistance=8
    inWorldIconScale=1.0
    inWorldNameScale=1.0
}
```

## Known implementation difference from modern Xaero

Minecraft 1.7.10 Xaero has no native Tracked Players API. The map portion therefore uses GTNH Navigator's Xaero layer
API rather than injecting a native Xaero tracked-player object. Behaviour is intentionally matched, but the settings do
not appear inside Xaero's own GUI.

The remaining render differences are documented in `docs/TARGET_PARITY_0.3.2.md`. In particular, Navigator does
not expose the current round/square minimap shape, so edge clamping remains radial. When the compatibility switch
`useXaeroRadarForLoadedPlayers=true` is selected explicitly, the old renderer transition and its legacy-Xaero visual
differences also return.


## 0.2.2 compatibility fixes

- Removed the custom overrides of Navigator's button lifecycle methods. This keeps the addon source compatible whether
  the target Navigator build exposes those methods as overridable or final.
- The map/world hotkey state is now written to `teamtrackergtnh.cfg` immediately.
- The in-world overlay no longer renders while Minecraft's GUI is hidden with F1.
- Project and runtime version metadata now agree on 0.2.2.

## 0.2.1 review fixes

The 0.2.0 test jar had two POV-only defects discovered during code review: an incorrect camera-space assumption in `RenderWorldLastEvent` and zeroed GL constants in the manual test build. Do not use 0.2.0 for evaluating the in-world marker. 0.2.1 fixes both.
