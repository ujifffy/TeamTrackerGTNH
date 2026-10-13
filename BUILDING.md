# Building Team Tracker GTNH

The repository includes a Gradle wrapper and targets the standard GTNH 1.7.10
convention plugin. Use a network-enabled development environment for the first
build so Gradle, Forge/Minecraft mappings and the exact mod dependencies can be
resolved.

```bash
./gradlew clean build
```

The production/reobfuscated artifact is written under `build/libs/`. Do not ship
the historical `TeamTrackerGTNH-0.2.1-test.jar`; it was produced by a manual
stub-based build and is retained only as a forensic baseline in the handoff.

Before release, run both development launch configurations with the exact GTNH
2.9.0-beta-3 dependency set:

```bash
./gradlew runClient
./gradlew runServer
```

For the client smoke test, install the intended Xaero builds in the development
run directory:

- `Xaeros_Minimap_21.10.44_Forge_1.7.10.jar` (CurseForge file 6012805)
- `XaerosWorldMap_1.14.1.33_Forge_1.7.10.jar` (CurseForge file 5987124)
- Navigator 1.1.9
- ServerUtilities 2.4.9

Record results against `docs/TEST_MATRIX.md`; static inspection is not a
substitute for these launch and in-game tests.
