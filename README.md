# Team Tracker GTNH 0.3.1

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
- Inside the minimap frame, Xaero's ordinary player radar owns loaded players, preventing duplicate heads.
- When the teammate leaves the minimap frame, a tracked-player head is pinned to the minimap edge.
- When the teammate leaves vanilla entity tracking range, the server position automatically takes over.
- Remote server snapshots are interpolated instead of jumping every sync packet.
- All current-dimension teammates, including locally loaded players, remain visible on the fullscreen Xaero World Map with their skin head and name.
- In-world/POV markers use the same live-position-first rule and remain visible through terrain.
- On-map icon scale, in-world icon scale and in-world name scale are configurable in the same spirit as modern Xaero.
- Two unbound Controls keybinds reproduce modern Xaero's separate map/world tracked-player toggles and persist their state.
- The Navigator layer button uses Navigator's normal active/inactive state instead of overriding its lifecycle methods.
- In-world markers respect Minecraft's F1 / Hide GUI setting.

The optional distance text under the in-world marker is intentionally an extra convenience feature; it can be disabled.

## Safe upgrade

1. Stop the dedicated server cleanly. Forge 1.7.10 mods cannot be hot-swapped in a running JVM.
2. Replace the old TeamTrackerGTNH jar with `teamtrackergtnh-0.3.1.jar`; do not keep both versions.
3. Replace the jar in every client instance while Minecraft is closed.
4. Start the server and clients normally. Keep the existing world and `config/teamtrackergtnh.cfg`.

The mod owns no blocks, items, entities, dimensions, saved-world data or player NBT. Version 0.3.1 therefore has no
world migration. Existing config keys and values are retained, unknown config keys are preserved, and an internal
schema number is added for future migrations.

The network discriminator and the complete 0.2.x packet prefix stay unchanged. 0.3.x adds only an optional
length-prefixed extension and accepts remote wire versions in `[0.2.2,0.4.0)`. However, 0.2.x itself used FML's
default exact-version check, so a mixed 0.2.x/0.3.x connection is not guaranteed to pass the old side's handshake.
Perform the first upgrade from 0.2.x on the server and every client in one maintenance restart. The explicit range makes
future compatible 0.3.x rolling upgrades possible from this release onward.

0.3.1 changes only client rendering and config schema metadata. Its wire format is identical to 0.3.0, so a 0.3.0
server and 0.3.1 client can coexist temporarily while the jars are replaced.

## Important Xaero setting

For the closest match to GT Community Pack Modern, leave Xaero's normal **Entity Radar / Players** enabled.
The addon deliberately lets Xaero itself draw teammates that are already loaded inside the minimap frame, then takes
over only for out-of-frame / remotely tracked positions.

## Config

Generated at `config/teamtrackergtnh.cfg`.

Most relevant options:

```text
client.map {
    showMapHeads=true
    clampOffscreenMapHeads=true
    useXaeroRadarForLoadedPlayers=true
    interpolateRemotePlayers=true
    onMapIconScale=1.0
    minimapIconScale=1.5
    worldMapIconScale=2.5
    showWorldMapNames=true
    worldMapNameScale=0.75
}

client.world {
    showInWorldHeads=true
    showInWorldName=true
    showInWorldDistance=false
    inWorldOpacity=0.45
    inWorldNearOpacityMultiplier=0.35
    inWorldFullOpacityDistance=20
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

The remaining render differences are documented in `docs/TARGET_PARITY_0.3.1_RU.md`. In particular, the legacy
Navigator layer cannot observe modern Xaero's exact `wasRenderedOnRadar` handoff signal or the current round/square
minimap shape. Keep Xaero Entity Radar / Players enabled when
`useXaeroRadarForLoadedPlayers=true`; otherwise use `false` to guarantee the addon's own head is drawn, accepting a
possible duplicate when Xaero also draws that player.


## 0.2.2 compatibility fixes

- Removed the custom overrides of Navigator's button lifecycle methods. This keeps the addon source compatible whether
  the target Navigator build exposes those methods as overridable or final.
- The map/world hotkey state is now written to `teamtrackergtnh.cfg` immediately.
- The in-world overlay no longer renders while Minecraft's GUI is hidden with F1.
- Project and runtime version metadata now agree on 0.2.2.

## 0.2.1 review fixes

The 0.2.0 test jar had two POV-only defects discovered during code review: an incorrect camera-space assumption in `RenderWorldLastEvent` and zeroed GL constants in the manual test build. Do not use 0.2.0 for evaluating the in-world marker. 0.2.1 fixes both.
