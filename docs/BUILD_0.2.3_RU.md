# TeamTrackerGTNH 0.2.3 — отчёт сборки

Дата: 2026-10-05.

## Состав релиза

- `teamtrackergtnh-0.2.3.jar`
- `TeamTrackerGTNH-0.2.3-source.zip`

## Метод сборки

Gradle 9.3.1 отсутствовал в текущем runtime, а его внешняя загрузка была
недоступна. Поэтому релиз собран как минимальное обновление ранее проверенного
и полностью reobfuscated `0.2.2.jar`:

1. Изменённые классы скомпилированы `jdk.compiler` с `-source 8 -target 8`.
2. Minecraft-ссылки компилировались непосредственно в SRG namespace против
   сохранённого `srg_merged_minecraft.jar`.
3. Navigator и ServerUtilities брались из реальных JAR GTNH 2.9.0-beta-3:
   Navigator 1.1.9, ServerUtilities 2.4.9 и GTNHLib 0.11.46.
4. Узкие Forge/FML compile-only stubs использовались только для типов, которых
   не было в локальном classpath. Их имена и дескрипторы сверены с байткодом
   рабочего 0.2.2; ни один stub не упакован в релиз.
5. Пять полученных class-файлов заменили соответствующие классы в production
   reobf-JAR 0.2.2. Остальной проверенный production bytecode сохранён.
6. Версия обновлена до 0.2.3 в `mcmod.info`, runtime constant и исходниках.

Это не полный повторный Gradle `clean build`; для окончательного публичного
релиза его всё равно желательно выполнить, когда Gradle toolchain доступен.

## Проверки

- ZIP integrity: без ошибок.
- Все изменённые классы: Java major version 52 (Java 8).
- Minecraft-вызовы в изменённом байткоде используют SRG-имена 1.7.10.
- `SubscribeEvent` сохранён как runtime-visible annotation.
- В JAR отсутствуют `cpw/mods`, `net/minecraftforge`, `com/mojang/authlib` и
  `com/google/common` compile-only stubs.
- `mcmod.info`: версия 0.2.3, Minecraft 1.7.10, обязательные зависимости
  `serverutilities` и `navigator`.
- Архив исходников не содержит Gradle cache, generated Minecraft или stubs.

## SHA-256

- JAR: `fef920f24e6c0618f52a30d44f01fb778a6f6f09411316a4f34d94df023a4167`
