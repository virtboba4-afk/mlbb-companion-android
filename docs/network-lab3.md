# Лабораторная 3: работа с сетью

## REST API

Статистика героев берётся с бесплатного открытого API [MLBBHub](https://mlbbhub.com/agents.md)
(данные рейтинговых матчей из официальных источников Moonton, обновляются ежедневно, без ключа и регистрации).

```
GET https://mlbbhub.com/api/stats?limit=15&sort=winRate
```

```json
{
  "heroes": [
    {"slug": "masha", "winRate": 60.88, "pickRate": 0.54, "banRate": 36, "tier": "S", "winRateDelta": 1.04}
  ],
  "total": 133,
  "available": true,
  "lastUpdated": "2026-10-25T20:30:11.490Z"
}
```

## Реализация

| Что | Где |
|---|---|
| Разрешения `INTERNET`, `ACCESS_NETWORK_STATE` | `AndroidManifest.xml` |
| Retrofit 3 + конвертер Gson + логирование OkHttp | `gradle/libs.versions.toml`, `app/build.gradle.kts` |
| Модели JSON-ответа | `network/HeroStatsResponse.java`, `network/HeroStatsDto.java` |
| Описание запроса | `network/MlbbHubApi.java` |
| Настройка клиента (таймауты, логирование) | `network/ApiClient.java` |
| Репозиторий: порции, проверка сети, ошибки | `data/HeroRepository.java` |
| Запрос при открытии экрана и вывод результата | `ContentFragment.java` (RecyclerView + View Binding в `HeroAdapter`) |

## Порционная загрузка

Порция - 15 героев (`HeroRepository.PAGE_SIZE`). API поддерживает только параметр `limit`,
поэтому следующая порция запрашивается как `limit = offset + 15`, а в список добавляются последние 15 записей.
По полю `total` из ответа определяется, есть ли ещё данные: тогда в конце списка показывается кнопка
«Загрузить ещё», иначе - «Все герои загружены».

## Обработка ошибок

| Ситуация | Что видит пользователь |
|---|---|
| Нет подключения (или включён офлайн-режим в настройках) | «Нет подключения к интернету» |
| Таймаут / сервер недоступен (`IOException`) | «Сервер не отвечает. Попробуйте позже» |
| Код ответа не 2xx | «Сервер вернул ошибку {код}» |
| Некорректный JSON или `available = false` | «Сервер прислал некорректные данные» |
| Пустой список героев | «Сервер вернул пустой список героев» |

Если первая порция не загрузилась, экран показывает ошибку, кнопку «Повторить загрузку»
и, если есть резервная копия, кнопку «Использовать резервную копию».
Ошибка при догрузке следующей порции показывается в Snackbar с кнопкой повтора.
Незавершённый запрос отменяется в `onDestroyView`, чтобы ответ не пришёл на уничтоженный экран.

В LogCat запросы видны с тегом `okhttp.OkHttpClient`:

```
--> GET https://mlbbhub.com/api/stats?limit=15&sort=winRate
<-- 200 https://mlbbhub.com/api/stats?limit=15&sort=winRate (1367ms, unknown-length body)
--> GET https://mlbbhub.com/api/stats?limit=30&sort=winRate
<-- 200 https://mlbbhub.com/api/stats?limit=30&sort=winRate (139ms, unknown-length body)
```
