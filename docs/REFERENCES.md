# References already used during design/review

These are the most relevant upstream references. New Work should re-open exact tags/files as needed rather than trusting summaries blindly.

## GT Community Pack Modern target

Repository:
https://github.com/GregTechCEu/GregTech-Modern-Community-Pack

Known reference release used for behavior/config comparison:
https://github.com/GregTechCEu/GregTech-Modern-Community-Pack/releases/tag/v1.14.5

Relevant v1.14.5 files:
- `mods/xaeros-minimap.pw.toml`
- `mods/xaeros-world-map.pw.toml`
- `mods/ftb-teams-forge.pw.toml`
- `config/xaerominimap.txt`
- `config/xaeroworldmap.txt`
- `config/xaerominimap_entities.json`

Observed in that release:
- `displayTrackedPlayers:true`
- `trackedPlayers:true`
- entity category `tracked` with icons enabled

## Xaero Tracked Players semantics / settings names

Translations repository:
https://github.com/thexaero/xaero-minimap-translations

Relevant English keys around tracked players in `en_us.lang`:
- `gui.xaero_tracked_players`
- `gui.xaero_tracked_players_on_map`
- `gui.xaero_tracked_players_in_world`
- `gui.xaero_tracked_player_world_icon_scale`
- `gui.xaero_tracked_player_world_name_scale`
- `gui.xaero_tracked_player_minimap_icon_scale`
- toggle keys for map/world tracked players

## ServerUtilities exact API

Repo/tag:
https://github.com/GTNewHorizons/ServerUtilities/tree/2.4.9

Relevant file:
`src/main/java/serverutils/lib/data/ServerUtilitiesAPI.java`

Important public API:
- `arePlayersInSameTeam(UUID, UUID)`
- `getTeamID(UUID)`

Skin icon implementation:
`src/main/java/serverutils/lib/icon/PlayerHeadIcon.java`

Important behavior: PlayerHeadIcon can async-fetch Mojang skin texture by UUID if not already in TextureManager.

## Navigator exact API

Repo/tag:
https://github.com/GTNewHorizons/Navigator/tree/1.1.9

Relevant files:
- `api/NavigatorApi.java`
- `api/model/buttons/ButtonManager.java`
- `api/model/layers/LayerManager.java`
- `api/model/layers/UniversalLayerRenderer.java`
- `api/model/steps/UniversalRenderStep.java`
- `api/util/DrawUtils.java`
- `mixins/late/xaerosminimap/MinimapRendererMixin.java`
- `mixins/late/xaerosworldmap/GuiMapMixin.java`

Critical fact: `ButtonManager.activate()`, `.deactivate()`, `.toggle()` are final in 1.1.9.

Navigator minimap integration supplies `circleShape`, `minimapFrameSize`, Xaero rotation angle and scale internally, but the public consumer layer used here does not directly receive shape/angle as explicit API fields.

## GTNH 1.7.10 RenderWorldLast reference

GT5-Unofficial example:
https://github.com/GTNewHorizons/GT5-Unofficial

Relevant file:
`src/main/java/gregtech/client/GTWorkAreaRenderer.java`

Useful reference for 1.7.10 camera interpolation and explicit `glTranslated(-cameraX,-cameraY,-cameraZ)` in RenderWorldLastEvent.

## Modern third-party use of native Xaero tracked-player APIs

MapLink:
https://github.com/thebuildcraft/MapLink

Relevant classes include:
- `MiniMapPlayerTrackerReader`
- `WorldMapPlayerTrackerReader`
- `RemotePlayerTrackerSystem`
- player-tracker renderer mixins

This is useful as evidence of modern Xaero's native tracked player abstractions and as a behavioral reference, not code to copy blindly into 1.7.10.
