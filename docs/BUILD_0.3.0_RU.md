# TeamTrackerGTNH 0.3.0 — отчёт сборки

Дата: 2026-10-09.

## Состав релиза

- `teamtrackergtnh-0.3.0.jar`
- `TeamTrackerGTNH-0.3.0-source.zip`

## Изменения для upgradability

- world/NBT/registry migration не требуется: мод не сохраняет данные мира и не
  регистрирует блоки, предметы, сущности или измерения;
- ключи конфига 0.2.x сохранены, добавлен `internal.schemaVersion=1`;
- network channel/discriminator и legacy prefix пакета не изменены;
- новые поля находятся в versioned length-prefixed extension;
- клиент принимает пакет 0.2.x без extension;
- 0.2.3 decoder проверен на пакете 0.3.0 с extension;
- full snapshots имеют sequence; old/duplicate/downgrade packet не применяется;
- `acceptableRemoteVersions=[0.2.2,0.4.0)` задан начиная с 0.3.0.

Важно: старый 0.2.x JAR не объявлял диапазон и использовал FML exact-version
default. Поэтому первый переход на 0.3.0 выполняется одновременно на сервере и
клиентах, хотя сам формат пакета совместим в обе стороны.

## Метод сборки

Gradle 9.3.1 отсутствует локально, а загрузка distribution недоступна. Релиз
собран как минимальное обновление ранее проверенного reobf-JAR 0.2.3:

1. Изменённые классы скомпилированы `jdk.compiler` с `-source 8 -target 8`.
2. Minecraft-ссылки server tracker скомпилированы в SRG namespace против
   `srg_merged_minecraft.jar`.
3. Navigator 1.1.9, ServerUtilities 2.4.9 и GTNHLib 0.11.46 взяты из локальной
   официальной GTNH 2.9.0-beta-3 dependency set.
4. Узкие Forge/FML/Netty compile-only stubs использованы только при компиляции;
   ни один stub не упакован в JAR.
5. В production JAR заменены только `TeamTrackerGTNH`, `Config`,
   `TeamSyncPacket` (включая nested classes), `ServerTeamTracker` и
   `ClientTeamData`; набор JAR entries полностью совпадает с 0.2.3.

Это не полный повторный `./gradlew clean build`. Такой build и реальный запуск
двух клиентов остаются обязательными перед публичным распространением.

## Автоматические и статические проверки

- ZIP integrity: PASS.
- Все изменённые classes: Java major version 52 (Java 8).
- `@Mod`, `@SidedProxy`, `@Mod.EventHandler` и `@SubscribeEvent` присутствуют как
  runtime-visible annotations.
- `@Mod` и `mcmod.info` содержат версию 0.3.0.
- Server bytecode использует SRG 1.7.10 calls/fields.
- Compile-only packages отсутствуют в JAR.
- Набор entries JAR 0.2.3 и 0.3.0 одинаков.
- New encoder → new decoder: PASS.
- Legacy 0.2.x packet → new decoder: PASS.
- Future unknown extension → new decoder skip: PASS.
- Truncated/oversized packet rejection: PASS.
- New packet → actual 0.2.3 decoder: PASS; legacy entries прочитаны, extension
  оставлен непрочитанным.
- Client old/duplicate/downgrade snapshot rejection and reconnect reset: PASS.

## SHA-256

- JAR: `0b46a14a2bbfc806f327967325347e5071dd974e25078171b733afd7a13a0a45`
