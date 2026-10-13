# TeamTrackerGTNH 0.2.2 verification status

## Input integrity

- Handoff archive SHA-256 matched the supplied value:
  `a2a2db7fcdfa3dc28f6baea7e96f018bbf0b96cdf91755fc6417817837046dbe`.
- The 0.2.1 JAR and source ZIP also matched the hashes inside the handoff.

## Changes made

- Removed the `activate()`, `deactivate()` and `toggle()` overrides from the
  Navigator button implementation. The source therefore no longer depends on
  those methods being overridable.
- Connected Navigator's toggle callback to the persisted `showMapHeads` option.
- Persisted both map and in-world hotkey states to the Forge configuration.
- Made the POV renderer respect `gameSettings.hideGUI` (F1).
- Snapshotted ServerUtilities team IDs once per sync interval and grouped online
  players by team before producing receiver-specific packets.
- Corrected the target from ServerUtilities 2.4.13 to 2.4.9 after inspecting the
  official GTNH 2.9.0-beta-3 client archive.
- Added `arePlayersInSameTeam` as the final per-recipient authorization check.
- Synchronized project/runtime metadata at version 0.2.2.
- Added the Gradle wrapper and preserved the handoff audit/test documents.

## Checks completed here

- Downloaded and integrity-checked the official GTNH 2.9.0-beta-3 client
  archive.
- Confirmed its relevant runtime versions: Navigator 1.1.9,
  ServerUtilities 2.4.9 and GTNHLib 0.11.46.
- Compiled against the exact Navigator 1.1.9 and ServerUtilities 2.4.9 dev
  artifacts from the GTNH Maven repository.
- Ran `spotlessApply clean build` successfully through compilation,
  Checkstyle, packaging and reobfuscation with the GTNH Gradle convention.
- Reviewed all production Java sources and dependency declarations.
- Confirmed no client package is directly referenced by the dedicated-server
  proxy path; the `@SidedProxy` client target remains a class-name string.
- Confirmed the server sends only online, non-self players sharing the
  receiver's non-zero team ID and accepted by ServerUtilities' public
  `arePlayersInSameTeam` predicate.
- Confirmed map and world toggles remain independent.
- Confirmed the source no longer declares Navigator lifecycle overrides.
- Confirmed version strings and the documented default `inWorldMinDistance=0`
  are consistent.
- Inspected the production JAR: it contains no bundled dependency classes,
  declares Java 8 class files and calls the expected obfuscated 1.7.10 world
  lookup method.

## Produced artifact

- File: `teamtrackergtnh-0.2.2.jar`
- SHA-256:
  `04cc5b866e1a526fd6831dfd410ac67186e914122484d63833d772bdf489bcb4`

## Still requires in-game verification

The compile/reobfuscation release gate is passed. A real multiplayer client and
dedicated-server session with Xaero's Minimap/World Map is still required to
mark the visual, movement, party transition and long-distance scenarios in
`TEST_MATRIX.md` as passed.

The stock GTNH 2.9.0-beta-3 archive contains JourneyMap, not Xaero. Xaero must
therefore be installed separately on the test client; it is not bundled into
this mod.

The handoff's claim that Navigator 1.1.9 makes the three lifecycle methods
`final` was disproved by direct inspection of the JAR shipped in the official
GTNH 2.9.0-beta-3 client pack. `activate`, `deactivate` and `toggle` are
non-final; only `setOnToggle` and `setLayerNotify` are final. The 0.2.2 design
still avoids unnecessary lifecycle overrides and uses the supported callback.

## Remaining release gate

The Gradle server runner reached its interactive Minecraft EULA prompt. It was
stopped there because this work did not accept that agreement on the user's
behalf. After accepting the EULA locally, run the dedicated-server boot smoke
test and then execute the multiplayer visual scenarios in `docs/TEST_MATRIX.md`.
Do not mark those scenarios as passed based on static bytecode inspection alone.
