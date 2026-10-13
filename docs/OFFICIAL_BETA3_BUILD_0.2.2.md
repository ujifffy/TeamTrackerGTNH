# TeamTrackerGTNH 0.2.2 — official beta3 build provenance

## Target pack

- Official archive: `GT_New_Horizons_2.9.0-beta-3_Java_17-26.zip`
- Archive source: `https://downloads.gtnewhorizons.com/Multi_mc_downloads/betas/GT_New_Horizons_2.9.0-beta-3_Java_17-26.zip`
- Archive size: 719,551,573 bytes
- Archive test: `unzip -t` passed

Relevant runtime JARs inspected from the archive:

| Dependency | Version | SHA-256 |
|---|---:|---|
| Navigator | 1.1.9 | `4ec8bf3ff815b31715692839e81838c339fa0465916188cef5108f7107ba6dd5` |
| ServerUtilities | 2.4.9 | `78fa6191c4992fc2cbeb6736745c902acb4bf1962415f193ba9657a748829665` |
| GTNHLib | 0.11.46 | `967a19a6c58cdef80e363b161f3ac0ce762196d6c83da2cf3607b575c4676e92` |

The official beta3 pack contains JourneyMap. Xaero's Minimap and Xaero's World
Map are optional/manual client additions and are not bundled by this project.
The intended 1.7.10 files were verified on the authors' CurseForge projects as
`Xaeros_Minimap_21.10.44_Forge_1.7.10.jar` (file 6012805) and
`XaerosWorldMap_1.14.1.33_Forge_1.7.10.jar` (file 5987124).

- Minimap: `https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap/files/6012805`
- World Map: `https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map/files/5987124`

## Build

- Gradle: 9.3.1
- Build JDK: Eclipse Temurin 25.0.4.1
- Command: `gradle --no-daemon spotlessApply clean build`
- Result: successful compilation, Checkstyle, JAR packaging and reobfuscation
- Output bytecode: Java 8 (class major version 52)

The source compiles against the exact GTNH Maven dev artifacts for Navigator
1.1.9 and ServerUtilities 2.4.9. Runtime dependencies are declared
non-publishable and are not shaded into the output JAR.

## Release artifact

- File: `teamtrackergtnh-0.2.2.jar`
- Size: 31,219 bytes
- SHA-256:
  `04cc5b866e1a526fd6831dfd410ac67186e914122484d63833d772bdf489bcb4`

## Verification boundary

The build and production-bytecode checks are complete. In-game multiplayer
verification with a real dedicated server and a client carrying the intended
Xaero versions is still required for visual acceptance.
