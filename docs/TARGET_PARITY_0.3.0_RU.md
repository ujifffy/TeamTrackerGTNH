# TeamTrackerGTNH 0.3.0 — сравнение с целевым Xaero Tracked Players

Цель: поведение Xaero Tracked Players из GT Community Pack Modern, но источником
отношений служит ServerUtilities и показываются только игроки одной команды.

## Что совпадает по смыслу

| Область | 0.3.0 | Оценка |
| --- | --- | --- |
| Авторизация координат | Только online-члены той же ServerUtilities team; финальная проверка `arePlayersInSameTeam` | совпадает с заданием |
| Дальность | Сервер синхронизирует команду независимо от vanilla tracking distance | совпадает |
| Live/remote position | При наличии настоящего `EntityPlayer` берётся live position, иначе server snapshot | совпадает по данным |
| Измерения | Map/POV рисуются только в текущем dimension; смена dimension делает snap | безопасно |
| Выход/смена команды | Полный snapshot удаляет отсутствующих игроков | совпадает |
| Reconnect/server switch | Session generation не даёт старой очереди воскресить markers | исправлено |
| Остановка sync | Через 10 секунд stale markers очищаются | безопасное расширение |
| Порядок пакетов | 0.3.0 отклоняет старые и повторные sequenced snapshots | исправлено |
| Скины | Голова привязана к UUID, при недоступной texture действует fallback `PlayerHeadIcon` | функционально |

Мод является overlay и не меняет игровой мир/механику. В серверной gameplay-части
после повторного аудита дефектов не найдено; обнаруженные несовпадения относятся
к протоколу обновления (исправлены в 0.3.0) и к визуальному backport-слою.

## Что ещё не является parity 1:1

| Приоритет | Область | Целевой modern Xaero | Текущая 0.3.0 / риск |
| --- | --- | --- | --- |
| P0 render | Live ↔ remote handoff | Скрывает tracked marker только при фактическом `wasRenderedOnRadar` | Legacy Navigator не сообщает этот факт. При `useXaeroRadarForLoadedPlayers=true` и выключенном Entity Radar/Players live teammate может исчезнуть; при `false` возможен дубль |
| P0 render | Форма minimap | Clamp знает circle/square | Navigator layer не сообщает shape; используется радиальный clamp, поэтому на square map углы сдвигаются внутрь |
| P1 render | Minimap marker | Face 8×8 с белой рамкой около 10×10 | Face 10×10 с тёмной рамкой около 12×12 |
| P1 render | Fullscreen map | Около 30×30, имя/анимация по hover | 12 px, имя постоянно; hover/interaction нет |
| P1 render | POV | Screen-space marker; скрыт ближе 10 блоков, fade 10–20, имя при наведении/crosshair | World-space billboard, clamp-depth 8, min distance по умолчанию 0, постоянная opacity 0.45 и постоянное имя |
| P1 render | Отдельные режимы | Отдельные настройки Minimap/World Map/POV | Один map toggle управляет minimap и fullscreen, второй — POV |
| P1 render | Частота | Delta add/remove до ~250 мс | Полный snapshot обычно каждые 500 мс с интерполяцией |
| P2 render | World Map features | Dimension scaling, hover/right-click/teleport integration | Только marker/name текущего dimension |
| P2 platform | Stencil | Native Xaero path | Navigator minimap overlay зависит от stencil; при несовместимом renderer/driver слой может отключиться |

Mutual allies намеренно не реализованы: пользовательское требование — только
участники одной ServerUtilities team.

## Обязательный runtime acceptance-test

1. Dedicated server и два клиента: 0–20, 100 и 1000+ блоков.
2. Круглая и квадратная minimap: центр, стороны и четыре угла.
3. Entity Radar/Players включён и выключен; проверить исчезновения и дубли.
4. Вход/выход vanilla tracking range, телепорт, смерть, respawn, dimension change.
5. Fullscreen map на нескольких zoom; skin, name, overlap нескольких игроков.
6. POV через стены, F1, GUI, first/third person, близкая дистанция и смена FOV.
7. Logout, выход из команды, reconnect и быстрый переход на другой сервер.
8. Обновление 0.2.3 → 0.3.0 на копии сервера без удаления world/cfg.
9. В логах не должно быть FML handshake, packet decode, `NoSuchMethodError`,
   texture или OpenGL ошибок.
