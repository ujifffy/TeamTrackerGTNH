# TeamTrackerGTNH 0.3.1 — отчёт сборки

Дата: 2026-10-13.

## Состав релиза

- `teamtrackergtnh-0.3.1.jar`
- `TeamTrackerGTNH-0.3.1-source.zip`

## Исправления

- minimap marker: 10 → 15 px при стандартных scale;
- World Map marker: 12 → 30 px при стандартных scale;
- добавлены независимые `minimapIconScale=1.5` и
  `worldMapIconScale=2.5`;
- POV opacity больше не применяется дважды через OpenGL color и vertex alpha;
- alpha test отключён внутри изолированного POV render-state;
- второй head layer (hair/hat) явно рисуется после base face;
- добавлена smoothstep-кривая прозрачности рядом с игроком;
- config schema повышена с 1 до 2 без изменения существующих значений;
- network/server state полностью сохранены от 0.3.0.

## Метод сборки

Gradle 9.3.1 отсутствует локально, а загрузка distribution недоступна. 0.3.1
собрана как минимальный SRG-compatible patch production JAR 0.3.0. Заменены
ровно четыре class-файла и `mcmod.info`:

- `TeamTrackerGTNH.class`;
- `Config.class`;
- `TeamPlayerRenderStep.class`;
- `InWorldTeamRenderer.class`;
- `mcmod.info` содержит только новую версию metadata.

Остальные class/resources побайтно совпадают с 0.3.0. Compile-only stubs не
упакованы.

## Проверки

- ZIP integrity: PASS.
- Изменённые classes: Java major version 52.
- `@Mod` и `@SubscribeEvent` сохранены как runtime-visible annotations.
- Runtime и `mcmod.info`: версия 0.3.1.
- Minecraft references в render bytecode используют SRG names 1.7.10.
- Minimap formula: `10 × onMapIconScale × 1.5 = 15 px`.
- World Map formula: `12 × onMapIconScale × 2.5 = 30 px`.
- Base UV: `(0.125, 0.25)–(0.25, 0.5)`.
- Overlay UV: `(0.625, 0.25)–(0.75, 0.5)`, второй draw после base.
- OpenGL color alpha во время head draw равна 1; marker alpha применяется
  ровно один раз в vertex color.
- POV opacity curve test: PASS (0 / 10 / 20 / 1000 blocks).
- Compile-only packages в JAR отсутствуют.

Полный `./gradlew clean build` и реальный GL-тест остаются внешними acceptance
steps, потому что runtime не содержит Gradle distribution и GTNH client process.

## SHA-256

- JAR: `ab78f337fe68d06c79df8a710ceeb4c83160566de06726eeb4319e756c2ac1f5`
