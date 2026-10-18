# TeamTrackerGTNH 0.3.2 — отчёт сборки

Дата: 2026-10-18.

## Состав релиза

- `teamtrackergtnh-0.3.2.jar`;
- `TeamTrackerGTNH-0.3.2-source.zip`.

## Исправления

- один TeamTracker renderer обслуживает loaded и remote игроков на minimap;
- старый native-radar handoff оставлен только как выключенный compatibility switch;
- minimap marker увеличен с 15 до 20 px при стандартных scale;
- POV marker полностью скрыт до 16 блоков и плавно появляется на 16–32;
- POV nickname выключен по умолчанию;
- base face и outer head layer (hair/hat/glasses) рисуются двумя отдельными
  blended-pass с одним alpha;
- config schema 3 мигрирует визуальные дефолты и сохраняет нестандартные scale/
  fade values и неизвестные ключи;
- wire-format и серверное состояние не изменены.

## Метод сборки

Gradle 9.3.1 не удалось загрузить из изолированной среды. Поэтому 0.3.2 собрана
тем же минимальным SRG-compatible способом, что 0.3.1: production JAR 0.3.1
использован как база, а изменённые client/config classes скомпилированы в Java 8
bytecode против точных API Navigator 1.1.9 и ServerUtilities 2.4.9.

Изменены ровно:

- `Config.class`;
- `TeamPlayerRenderStep.class`;
- `InWorldTeamRenderer.class`;
- version constant/annotation в `TeamTrackerGTNH.class`;
- `mcmod.info`.

Остальные файлы production JAR совпадают с 0.3.1. Compile-only stubs и dev JAR
зависимостей в релиз не упакованы.

## Проверки

- ZIP integrity: PASS.
- Набор файлов JAR совпадает с 0.3.1: PASS.
- Все classes имеют Java major version 52: PASS.
- `@Mod`, `@SubscribeEvent`, network discriminator и accepted range
  `[0.2.2,0.4.0)` сохранены.
- Runtime constant и `mcmod.info`: 0.3.2.
- Minecraft references изменённых renderer classes используют SRG names 1.7.10.
- Точные dev API: ServerUtilities 2.4.9 и Navigator 1.1.9.
- Config migration schema 2 → 3: PASS для старых defaults и custom scale/fade.
- POV curve test: PASS на 0 / 8 / 16 / 20 / 24 / 28 / 32 / 64 блоках.
- Minimap formula: `10 × onMapIconScale × 2.0 = 20 px`.
- Base UV: `(0.125, 0.25)–(0.25, 0.5)`.
- Outer-layer UV: `(0.625, 0.25)–(0.75, 0.5)`, второй draw после base.
- Эти UV дополнительно совпадают с реализацией `PlayerHeadIcon.draw` из
  ServerUtilities 2.4.9.
- Compile-only packages в JAR отсутствуют.

Полный `./gradlew clean build` и фактический GL-тест в клиенте GTNH остаются
внешними acceptance steps: Gradle distribution и Minecraft process недоступны
в среде сборки.

## SHA-256

- JAR: `b6c1992a3ad08d862b90058e36c875ce186027fd17c470a65bc66d9c910adc7d`
