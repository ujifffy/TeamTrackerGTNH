# TeamTrackerGTNH 0.3.2 — повторная проверка рендера

Версия 0.3.2 сохраняет серверную логику и wire-format 0.3.0/0.3.1. Изменения
ограничены клиентским рендером и schema 3 конфигурации.

## Исправлено

| Область | 0.3.1 | 0.3.2 |
| --- | --- | --- |
| Загруженный игрок на minimap | TeamTracker прекращал draw и передавал точку legacy Xaero Entity Radar | TeamTracker по умолчанию продолжает рисовать ту же голову; ручной compatibility switch сохранён |
| Размер minimap head | 15 px | 20 px; отдельный `minimapIconScale=2.0` |
| POV вблизи | Ненулевая прозрачность даже в упор | 0 alpha до 16 блоков, smoothstep fade 16–32, затем `inWorldOpacity` |
| POV name | Включён | `showInWorldName=false` по умолчанию |
| Outer head layer | Второй UV-pass | Второй UV-pass подтверждён; blend и render state изолированы |
| Config | schema 2 | schema 3 с миграцией визуальных дефолтов |

UV-развёртка `PlayerHeadIcon` остаётся стандартной для нормализованной текстуры
64×32: лицо `(0.125, 0.25)–(0.25, 0.5)`, внешний слой головы
`(0.625, 0.25)–(0.75, 0.5)`. Волосы, шляпа и очки рисуются вторым blended-pass
поверх лица с тем же alpha, поэтому прозрачность не возводится в квадрат.

## Миграция 0.3.1 → 0.3.2

- старый default `minimapIconScale=1.5` становится `2.0`; нестандартное значение сохраняется;
- `useXaeroRadarForLoadedPlayers` однократно устанавливается в `false`;
- старый default `inWorldFullOpacityDistance=20` становится `32`; нестандартное значение сохраняется;
- добавляется `inWorldHiddenDistance=16`;
- `showInWorldName` однократно устанавливается в `false`, после чего его можно снова включить вручную;
- неизвестные ключи Forge Configuration не удаляет.

## Оставшиеся отличия от modern Xaero

- Navigator не сообщает форму minimap, поэтому edge clamp остаётся радиальным.
- World Map имя показывается постоянно, без native hover-анимации.
- POV остаётся camera-facing world billboard, а не полным портом modern
  screen-space renderer/crosshair hover logic.
- Если вручную включить `useXaeroRadarForLoadedPlayers=true`, legacy native
  handoff и связанные с ним визуальные различия возвращаются.
