# TeamTrackerGTNH — полное ТЗ, архитектура и audit текущей 0.2.1

## 1. Контекст и цель

Среда: приватный сервер **GregTech: New Horizons 2.9.0-beta-3**, Minecraft 1.7.10. Игроки уже используют **ServerUtilities party** как аналог FTB Teams и **Xaero Minimap / Xaero World Map**.

Пользователь пришёл из GregTech Community Pack Modern (1.20.x) и хочет вернуть конкретный UX: участник команды виден как skin-head на карте даже далеко, а также есть in-world / POV marker. В GCP Modern это обеспечивается современным **Xaero Tracked Players**, который получает relation/позицию от server-side team integration (в паке используется FTB Teams).

Наша задача — не создавать произвольный локатор, а **максимально воспроизвести Xaero Tracked Players UX на 1.7.10**, заменив FTB Teams на ServerUtilities party.

## 2. Точные целевые зависимости

Базовая целевая комбинация, под которую писалась 0.2.1:

- Minecraft 1.7.10
- Forge 10.13.4.1614
- GTNH 2.9.0-beta-3
- ServerUtilities 2.4.13
- Navigator 1.1.9
- Xaero's Minimap 1.7.10 branch; в текущем README проекта зафиксирован intended build `21.10.44`
- Xaero's World Map 1.7.10 branch; в текущем README проекта зафиксирован intended build `1.14.1.33`
- Java runtime GTNH: Java 17 допустим для самого пака; мод должен оставаться совместимым с ожидаемым байткодом 1.7.10/GTNH (текущий test jar — Java 8 classfile major 52).

Точные Xaero filenames/versions надо ещё раз проверить по фактическому `mods/` пользователя/manifest beta-3 перед финальным релизом. Не повышать их самовольно.

## 3. Эталон: что именно копируем из GCP Modern

У проверенного GCP Modern release v1.14.5 были:

- Xaero Minimap 25.2.6 Forge 1.20
- Xaero World Map 1.39.9 Forge 1.20
- FTB Teams 2001.3.1
- `config/xaerominimap.txt`: `displayTrackedPlayers:true`
- `config/xaeroworldmap.txt`: `trackedPlayers:true`
- entity radar category `tracked` с player icons enabled

Современные Xaero translations явно описывают:

- **Display Tracked Players** — players whose locations are shared with you (usually your party), включая случай, когда они вышли за minimap bounds или иначе не видны;
- **On-Map Tracked Players**;
- **In-World Tracked Players**;
- **Tracked Player In-World Scale**;
- **Tracked Player Name Scale**;
- **Tracked Player On-Map Scale**;
- отдельные toggles для on-map и in-world tracked players.

Именно это — функциональная референсная модель.

## 4. Функциональные требования

### 4.1 Party/security

Обязательное:

- Источник relation — **ServerUtilities party**, не BetterQuesting и не отдельный friends-list.
- Игрок видит координаты только **online** участников своей party.
- Сам игрок в packet не включается.
- Игрок без party не получает координаты других игроков.
- Никаких client-side запросов «дай координаты UUID X»; сервер сам формирует разрешённый список.
- Join/leave party должен отражаться автоматически не позднее следующего sync.
- Logout teammate должен убрать marker не позднее следующего sync.

Предпочтительно использовать публичную semantics API `ServerUtilitiesAPI.arePlayersInSameTeam(UUID, UUID)` либо доказать корректность использования team UID. Текущий код использует `getTeamID()` и сравнение numeric ID.

### 4.2 Network sync

Текущая модель:

- server tick END;
- дефолт `syncIntervalTicks=10` (0.5 s);
- пакет для каждого receiver: UUID, name, dimension, x/y/z всех online teammates;
- count ограничен 512;
- client application переносится на Minecraft client thread.

Требования:

- маленькая нагрузка для 2–нескольких игроков;
- не отправлять посторонних;
- не полагаться на то, что vanilla entity остаётся tracked;
- remote movement должен быть визуально smooth;
- телепорты/respawn/portal не должны «размазываться» интерполяцией на сотни блоков.

Текущий 0.2.1 snap threshold: 128 blocks между server snapshots.

### 4.3 Live ↔ remote handoff

Ключевое требование, чтобы ощущалось как modern Xaero:

1. Если teammate существует в текущем `WorldClient` как настоящий `EntityPlayer`, использовать **live client entity position**.
2. Когда vanilla перестаёт присылать entity из-за distance/tracking, автоматически переключиться на server snapshot.
3. При возвращении entity — обратно на live source без ручного переключения.
4. Не рисовать две головы поверх друг друга с обычным Xaero radar.

Текущий `TrackedPlayer.getLiveEntity()`:

- смотрит current world по UUID;
- требует same dimension;
- исключает self и `isDead`;
- map/POV position предпочитает live entity;
- remote path линейно интерполирует snapshots.

### 4.4 Xaero minimap

Цель:

- Loaded teammate внутри minimap: визуально должен выглядеть как обычный игрок Xaero Entity Radar.
- Teammate покинул minimap frame, но ещё может быть vanilla-loaded: tracked head остаётся на краю/рамке в направлении teammate, если обычный radar уже не даёт эквивалентный UX.
- Teammate вышел за vanilla entity tracking range: marker остаётся и идёт по server position.
- Skin head, а не абстрактная точка/стрелка.
- Не должно быть дубля radar + tracker.
- Должен быть configurable icon scale.
- Rotation/North lock должны давать правильное направление.
- Circle и square minimap должны вести себя естественно.

Текущая 0.2.1 реализация:

- Navigator `UniversalLayerRenderer`;
- `generateVisibleLocations()` возвращает всех teammates current dimension, даже вне viewport;
- `TeamPlayerRenderStep` пропускает свой marker для live entity внутри bounds при `useXaeroRadarForLoadedPlayers=true`;
- outside radius marker радиально clamped;
- scale `onMapIconScale`;
- skin через `ServerUtilities PlayerHeadIcon`.

**Известное расхождение:** clamp сейчас круговой/radial. Для square minimap это не точное пересечение с квадратной рамкой и может выглядеть слишком «внутри» около углов.

**Нужно проверить в реальной 1.7.10 Xaero:** обычный Entity Radar может сам иметь out-of-bounds/frame behaviour. Если он уже рисует live player на рамке, текущая ветка `live && outOfBounds => наш marker` может дать дубль. Нужен реальный тест и/или чтение конкретного Xaero build.

### 4.5 Xaero fullscreen World Map

Цель:

- Remote teammate текущей dimension виден head + name.
- Нет дубля с live player display.
- Position обновляется smoothly.
- Name/icon scale близок к modern Xaero.
- В идеале marker интерактивен как native tracked player, но это secondary goal; не ломать карту ради right-click functionality.

Текущая 0.2.1:

- live entity при `useXaeroRadarForLoadedPlayers=true` пропускается;
- remote player рисуется через Navigator universal layer;
- head size 12 * scale;
- optional name, default scale 0.75.

Нужно подтвердить, что конкретный Xaero World Map 1.7.10 действительно показывает live radar players так, как предполагает suppress logic. Если нет — нужен fallback.

### 4.6 In-world / POV marker

Это отдельное обязательное требование пользователя, не только карта.

Желаемое поведение:

- В обычном first-person POV есть skin-head teammate в направлении его реального положения.
- Marker полупрозрачный (default target around 0.45 alpha).
- Видим через terrain/walls.
- Имя показывается.
- Distance может показываться как дополнительная опция; по умолчанию OFF, потому что это extension сверх базового target.
- Same dimension only.
- Не считать meaningful direction между dimensions.
- Scale головы и имени configurable.
- Дальний teammate не должен превращаться в микроточку.
- Toggle in-world независимо от map toggle.

Текущий 0.2.1 renderer:

- Forge `RenderWorldLastEvent`;
- camera = `mc.renderViewEntity`;
- camera position interpolated by partialTicks;
- target position = live entity position if available, otherwise interpolated remote position;
- target vertical point = `player feet Y + 1.55`;
- marker лежит на ray от camera eye к teammate;
- depth = `min(actualDistance, inWorldMarkerDistance)`; default max marker distance 8 blocks;
- billboard via `RenderManager.playerViewY/playerViewX`;
- `GL_DEPTH_TEST` disabled, depth mask false, blend enabled;
- head + hat layer UV;
- default opacity 0.45;
- default minimum distance 0.

Важно: эта renderer-модель — **наша approximation** modern Xaero. Не считать доказанным, что modern Xaero использует точно такой же fixed/clamped depth. Если можно посмотреть native behaviour, сравнить визуально и поправить.

Текущие известные POV edge cases для проверки:

- behind camera: 3D billboard окажется за камерой и не будет screen-edge arrow; решить, совпадает ли это с target;
- third-person back/front camera;
- camera mods / detached renderViewEntity;
- `F1` / `gameSettings.hideGUI`: текущий renderer явно это не проверяет и может остаться видимым при hidden HUD;
- chat/inventory/other GUI: текущий renderer полностью выключается при `currentScreen != null`; выяснить, совпадает ли это с Xaero;
- Angelica/shaders/OpenGL state;
- portal/bed/death transitions;
- очень близкий teammate и near plane;
- teammate строго сверху/снизу;
- translucent text/head readability.

## 5. Конфиг 0.2.1

`config/teamtrackergtnh.cfg`

Server:

- `syncIntervalTicks=10` (2..100)

Client map:

- `showMapHeads=true`
- `clampOffscreenMapHeads=true`
- `useXaeroRadarForLoadedPlayers=true`
- `interpolateRemotePlayers=true`
- `onMapIconScale=1.0`
- `showWorldMapNames=true`
- `worldMapNameScale=0.75`

Client world:

- `showInWorldHeads=true`
- `showInWorldName=true`
- `showInWorldDistance=false`
- `inWorldOpacity=0.45`
- `inWorldMinDistance=0`
- `inWorldMarkerDistance=8`
- `inWorldIconScale=1.0`
- `inWorldNameScale=1.0`

Keybinds (unbound by default):

- Toggle Tracked Players On Map
- Toggle Tracked Players In World

**Текущее отличие от Xaero:** keybind toggles меняют только static config в памяти и не сохраняют значение в cfg. После restart state вернётся к файлу. Желательно исправить.

## 6. Текущая архитектура по классам

- `TeamTrackerGTNH` — @Mod, channel, proxies.
- `CommonProxy` — loads config, registers client-bound packet type, server tick tracker.
- `ServerTeamTracker` — каждые N ticks строит same-team player list каждому receiver.
- `TeamSyncPacket` — UUID/name/dim/x/y/z list.
- `ClientProxy` — Navigator layer, keybinds, world renderer, lifecycle; packet scheduled onto client thread.
- `ClientTeamData` — mutable map UUID -> TrackedPlayer; membership and cache invalidation.
- `TrackedPlayer` — live entity lookup + remote snapshot interpolation + teleport snap.
- `TeamLayerManager` — Navigator layer; returns all current-dimension teammates.
- `TeamPlayerRenderStep` — minimap/worldmap head rendering and map clamp.
- `InWorldTeamRenderer` — independent 3D/POV billboard.
- `PlayerHeadIcon` берётся из ServerUtilities.

## 7. КРИТИЧЕСКИЙ статус текущего 0.2.1 test jar

### CRITICAL-1: ABI mismatch с реальным Navigator 1.1.9

`source/.../AlwaysOnTeamButton.java` объявляет:

- `activate()`
- `deactivate()`
- `toggle()`

и переопределяет их.

В реальном `Navigator 1.1.9` `ButtonManager` объявляет эти методы **final**.

Следствие: source не должен компилироваться против настоящего dependency API, а вручную собранный jar потенциально падает при class loading (`VerifyError` / `IncompatibleClassChangeError`-class family).

Это первый blocker. Исправить дизайн button/layer activation, затем собирать против настоящего Navigator dev artifact.

### CRITICAL-2: текущий бинарник собран не нормальным ForgeGradle production pipeline

0.2.1-test.jar получен ручной/stub-based сборкой и SRG-подменой. Ранее 0.2.0 именно из-за этого содержал неверные GL constants и не поймал API finality.

Поэтому:

- не использовать текущий test jar как release;
- не делать вывод «раз javap выглядит нормально, значит работает»;
- нужен clean build против реальных dependencies + reobfJar/production remap;
- желательно runClient и dedicated runServer smoke test.

### Что в 0.2.1 binary всё же уже проверено

См. `CURRENT_JAR_INSPECTION.md`: Java 8 major 52, SRG refs, correct GL numeric constants, no bundled Minecraft/Navigator/ServerUtilities stub classes. Это полезно, но не отменяет CRITICAL-1/2.

## 8. Остальные тонкие места / потенциальные улучшения

### Party API

Текущий код сравнивает `short teamId = ServerUtilitiesAPI.getTeamID(uuid)`. Публичный API также предоставляет `arePlayersInSameTeam(UUID, UUID)`. Для correctness/semantics лучше решить, что использовать. Numeric UID comparison, вероятно, работает, но новый Work должен проверить `ForgePlayer/team` semantics и отсутствие edge cases с team 0.

### Server complexity

Сейчас O(N^2) по online players: для каждого receiver повторно перебираются все candidates и дважды читается team ID. Для маленького private server это не проблема. Если хочется аккуратно — раз в sync сгруппировать online players по team UID.

### Stale timeout

TCP packet loss как таковой не проблема, но если server-side tracker перестал слать updates из-за exception/feature failure, client держит последнюю позицию бесконечно. Можно хранить last packet timestamp и скрывать marker после разумного timeout. Disconnect уже очищает state.

### Skin path

`ServerUtilities PlayerHeadIcon` при отсутствующей texture запускает async download через Mojang sessionserver по UUID. Это хорошо для remote player, но:

- offline-mode/non-Mojang UUID -> возможен default skin;
- недоступен sessionserver -> default skin;
- для live teammate POV можно потенциально использовать уже загруженный vanilla skin, чтобы не зависеть от HTTP;
- `InWorldTeamRenderer.clearCache()` чистит только map UUID->PlayerHeadIcon, но не удаляет texture из Minecraft `TextureManager`, поэтому это не полный skin texture purge.

### Map clamp / Xaero shape

Navigator 1.1.9 передаёт minimap frame size и применяет Xaero rotation/stencil, но layer API напрямую не даёт текущую shape. Radial clamp корректен для circle, но approximation для square. Work может:

- проверить возможности конкретного Xaero 1.7.10 API/reflection;
- вычислять square intersection, если shape можно узнать;
- или оставить documented approximation, если вмешательство слишком хрупкое.

### Radar dependency

При `useXaeroRadarForLoadedPlayers=true` наш addon сознательно не рисует loaded player внутри minimap. Если пользователь выключил Xaero Entity Radar/Players, teammate может исчезнуть внутри frame. Варианты:

- runtime detect radar/player category visibility и fallback на свой marker;
- всегда рисовать own marker и точно подавлять duplicates иным способом;
- documented requirement «Entity Radar Players must be ON» — это текущий простой вариант, и он соответствует GCP Modern config.

### Navigator button semantics

Даже после удаления illegal final overrides нужно продумать, нужен ли layer button вообще. `LayerManager.isLayerActive()` уже overridden to always true, а map/world visibility управляется собственными keybind/config. Не допустить, чтобы нажатие Navigator button деактивировало другие Navigator layers.

### Cross-dimension

Текущая визуализация скрывает teammates из других dimensions. Это сознательное решение. Если modern Xaero показывает другой-dimension status marker, это можно добавить позже, но нельзя рисовать бессмысленный world bearing между dimensions.

### Multiple teammates

Код рассчитан на collection, а не только на 2 игроков. Проверить overlap labels/heads и performance.

### HUD hiding

Добавить проверку `mc.gameSettings.hideGUI`, если target Xaero скрывается на F1.

### Config persistence

Hotkeys должны по возможности сохранять toggle state либо иметь отдельный persistent client state.

## 9. История итераций

### 0.1.0

- server party sync;
- map head + basic POV marker;
- без полноценного modern-Xaero handoff/edge behaviour.

### 0.2.0

- live vs remote handoff;
- minimap edge clamp;
- remote interpolation;
- map/world toggles/scales.

После review были найдены два POV-only дефекта test build:

- неверная camera-space assumption в `RenderWorldLastEvent`;
- GL constants в manual stub binary оказались нулевыми.

### 0.2.1

- camera transform через interpolated `renderViewEntity`;
- правильные GL constants;
- `try/finally` GL state restore;
- dead live entity reject;
- observed packet interval interpolation;
- >128 block same-dim teleport snap;
- client state/head object cleanup on reconnect;
- default `inWorldMinDistance=0`.

После упаковки 0.2.1 найден **новый критический ABI issue с final methods Navigator**. Поэтому 0.2.1 остаётся только reference snapshot.

## 10. Definition of Done

Версию можно считать реально пригодной, когда одновременно выполнено:

- Clean compile против точных GTNH dependency artifacts.
- Production/reobf jar построен воспроизводимо.
- Client starts without VerifyError/NoSuchMethodError/ClassNotFound.
- Dedicated server starts без client-class loading crash.
- Same-party sync работает; no-party и other-party не получают координаты.
- Loaded teammate inside minimap: один marker, не два.
- Out-of-frame same-dim teammate: marker остаётся на edge корректно при rotating/north-locked minimap.
- Beyond render distance: marker остаётся, remote motion smooth.
- Return into render distance: seamless handoff обратно на live entity.
- Fullscreen world map: head+name корректны, no duplicate.
- POV first-person: translucent skin head реально виден в направлении teammate и через wall.
- Dimension change: old bearing disappears; no interpolation across dimensions.
- Teleport/respawn: no long smear.
- Logout/party leave/reconnect: stale markers disappear.
- F1/GUI/third-person behaviour принято осознанно и протестировано.
- No persistent GL corruption with Angelica/normal rendering.
- Skin fallback behaviour известно и приемлемо.
