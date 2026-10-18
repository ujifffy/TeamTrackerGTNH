# TeamTrackerGTNH 0.3.2 — build report

Date: 2026-10-18.

## Release artifacts

- `teamtrackergtnh-0.3.2.jar`
- `TeamTrackerGTNH-0.3.2-source.zip`

## Changes

- One TeamTracker renderer now handles both loaded and remote players on the minimap.
- The previous native-radar handoff remains available only as an opt-in compatibility switch.
- The default minimap marker is increased from 15 to 20 px.
- The POV marker is fully hidden through 16 blocks and fades in from 16 to 32 blocks.
- The POV player name is disabled by default.
- The base face and outer head layer (hair, hats, glasses) are rendered in two separate blended passes with one alpha value.
- Config schema 3 migrates the visual defaults while preserving custom scale/fade values and unknown keys.
- The wire format and server state are unchanged.

## Build method

Gradle 9.3.1 could not be downloaded from the isolated build environment. Version 0.3.2 was therefore built with the
same minimal SRG-compatible procedure as 0.3.1: the 0.3.1 production JAR was used as the base, and the changed client
and config classes were compiled to Java 8 bytecode against the exact Navigator 1.1.9 and ServerUtilities 2.4.9 APIs.

Exactly these production files changed:

- `Config.class`
- `TeamPlayerRenderStep.class`
- `InWorldTeamRenderer.class`
- the version constant/annotation in `TeamTrackerGTNH.class`
- `mcmod.info`

All other files in the production JAR match 0.3.1. Compile-only stubs and development dependency JARs are not packaged.

## Verification

- ZIP integrity: PASS.
- JAR file set matches 0.3.1: PASS.
- All classes use Java major version 52: PASS.
- `@Mod`, `@SubscribeEvent`, the network discriminator and accepted range `[0.2.2,0.4.0)` are preserved.
- Runtime constant and `mcmod.info`: 0.3.2.
- Minecraft references in the changed renderer classes use 1.7.10 SRG names.
- Exact development APIs: ServerUtilities 2.4.9 and Navigator 1.1.9.
- Config schema 2 → 3 migration: PASS for previous defaults and custom scale/fade values.
- POV opacity curve: PASS at 0 / 8 / 16 / 20 / 24 / 28 / 32 / 64 blocks.
- Minimap formula: `10 × onMapIconScale × 2.0 = 20 px`.
- Base UV: `(0.125, 0.25)–(0.25, 0.5)`.
- Outer-layer UV: `(0.625, 0.25)–(0.75, 0.5)`, drawn after the base face.
- These UV coordinates also match `PlayerHeadIcon.draw` in ServerUtilities 2.4.9.
- No compile-only packages are present in the release JAR.

A complete `./gradlew clean build` and an actual GL test in the GTNH client remain external acceptance steps because
the Gradle distribution and Minecraft process are unavailable in the build environment.

## SHA-256

- JAR: `b6c1992a3ad08d862b90058e36c875ce186027fd17c470a65bc66d9c910adc7d`
