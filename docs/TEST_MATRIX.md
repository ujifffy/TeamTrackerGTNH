# TeamTrackerGTNH — test matrix

Статусы в этой таблице изначально `NOT TESTED`, пока конкретный Work/runClient/runServer не проверит их реально. Не подменять статический code review фактическим запуском.

| Area | Scenario | Expected result |
|---|---|---|
| Boot | Client with exact GTNH deps | No VerifyError / NoSuchMethodError / mixin or classloading crash |
| Boot | Dedicated server | Starts without loading client-only LWJGL/Xaero/Minecraft client classes |
| Dependency | Navigator 1.1.9 actual dev jar | Source compiles; no illegal final overrides |
| Dependency | ServerUtilities 2.4.9 actual dev jar | Team API calls resolve exactly |
| Network | User has no party | Empty list, no other coordinates |
| Network | Two players same party | Each receives the other only |
| Network | Third online player other party | Never appears in packet/markers |
| Network | Join party while online | Marker appears next sync |
| Network | Leave party while online | Marker disappears next sync |
| Network | Teammate logout | Marker disappears next sync |
| Network | Receiver reconnect | Old state is cleared before new packets |
| Network | Several teammates | All current-dimension markers render; no UUID/cache collisions observed |
| Map | Loaded teammate inside minimap, radar ON | Exactly one Xaero-looking head, native radar owns it |
| Map | Loaded teammate inside minimap, radar OFF | Decide desired fallback; current design may show nothing |
| Map | Loaded teammate exits minimap bounds | Head stays at edge; no duplicate with radar |
| Map | Remote teammate beyond render distance | Head remains visible |
| Map | Remote teammate moving | Smooth interpolation, no 0.5s jumps |
| Map | Teammate returns into render distance | Smooth handoff to live entity, no flicker/double icon |
| Map | Circular minimap | Edge clamp hugs circle |
| Map | Square minimap | Edge clamp matches square or documented approximation |
| Map | Rotating minimap | Direction remains correct during player yaw rotation |
| Map | North-locked minimap | Direction remains correct |
| Map | Enlarged minimap | Marker placement remains correct |
| World map | Remote teammate current dimension | Head + name at correct location |
| World map | Live teammate | Team layer draws head + name; no reliance on ordinary entity radar |
| World map | Zoom in/out | Icon/name scale behaves sensibly |
| World map | Huge coordinates | No precision/overflow artifacts |
| POV | First person, teammate in front | Translucent skin head + name visible |
| POV | Teammate behind wall | Marker remains visible through terrain |
| POV | Teammate behind camera | Behaviour understood/accepted; no random front projection |
| POV | Very far teammate | Marker remains readable, direction correct |
| POV | Very close teammate | No near-plane explosion/flicker |
| POV | Directly above/below | Billboard/direction stable |
| POV | Third-person back view | Marker orientation/position sensible |
| POV | Third-person front view | Marker orientation/position sensible |
| POV | F1 hideGUI | Should match chosen Xaero-like HUD behaviour; likely hide |
| POV | Chat/inventory GUI | Decide target; current code hides marker whenever currentScreen != null |
| POV | Death screen/respawn | No stale marker/camera glitch |
| POV | Bed/sleep/portal | No camera transform corruption |
| POV | Angelica/shaders | No GL state leak, broken textures, depth, culling or blend after render |
| Position | Same-dim ordinary motion | Interpolated remote path smooth |
| Position | Same-dim teleport >128 blocks | Snaps immediately, no smear |
| Position | Dimension change | Snaps dimension state; old POV marker disappears |
| Position | Nether/Overworld different dims | No meaningless direction marker |
| Skin | Normal Mojang UUID/network | Correct face + hat layer |
| Skin | Sessionserver unavailable | Safe fallback, no render crash |
| Skin | Offline-mode UUID | Safe fallback; document appearance |
| Toggle | Map keybind | Toggles map markers independently |
| Toggle | World keybind | Toggles POV independently |
| Toggle | Restart client | Decide/persist desired toggle state |
| Performance | 2 players | Negligible overhead |
| Performance | ~10-20 team members | No obvious packet/render issue; O(N^2) server behavior understood |
