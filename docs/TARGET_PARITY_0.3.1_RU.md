# TeamTrackerGTNH 0.3.1 — повторная проверка рендера

Версия 0.3.1 сохраняет серверную логику и wire-format 0.3.0. Изменения сделаны
по результатам реального клиентского теста minimap, World Map и POV.

## Исправлено

| Область | 0.3.0 | 0.3.1 |
| --- | --- | --- |
| Minimap head | 10 px × общий scale | 15 px × общий scale; отдельный `minimapIconScale=1.5` |
| World Map head | 12 px × общий scale | 30 px × общий scale; отдельный `worldMapIconScale=2.5` |
| POV skin overlay | Alpha задавался и OpenGL color, и вершинам; эффективная прозрачность возводилась в квадрат | Alpha задаётся один раз; волосы/шапка рисуются отдельным вторым проходом поверх лица |
| POV вблизи | Одинаковая opacity на любой дистанции | Smoothstep fade: 35% общей opacity в упор → 100% на 20 блоках |
| Config | schema 1 | schema 2; старые значения не меняются, новые ключи получают дефолты |

При стандартном `inWorldOpacity=0.45` фактическая opacity POV-маркера примерно:

- в упор: 0.158;
- на 10 блоках: 0.304;
- на 20 блоках и дальше: 0.45.

## Что остаётся отличием от native modern Xaero

- Legacy Navigator не сообщает точный `wasRenderedOnRadar`, поэтому minimap
  handoff по-прежнему зависит от `useXaeroRadarForLoadedPlayers`.
- Navigator layer не сообщает round/square shape; clamp остаётся радиальным.
- На World Map имя показывается постоянно, без native hover-анимации.
- POV остаётся camera-facing world billboard, а не полным портом modern
  screen-space renderer/crosshair hover logic.

## Настройка после теста

Если World Map head окажется слишком крупным, достаточно снизить
`worldMapIconScale` с 2.5 до 2.0. Общий `onMapIconScale` менять не нужно: он
одновременно влияет на minimap и World Map, а новые множители позволяют
подстраивать их независимо.
