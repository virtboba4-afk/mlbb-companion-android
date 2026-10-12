# Логи жизненного цикла (лабораторная работа 1)

Фильтр LogCat: `tag:Lifecycle`. Формат сообщений: `[название класса]: [название метода]`.
Логи сняты на эмуляторе Pixel 9a.

## Сценарий 1. Частичное перекрытие стартового экрана (шторка уведомлений)

Шторку выдвинули поверх StartActivity и закрыли. Activity остаётся видимой
и не уходит в `onPause`: она только теряет фокус окна (`onWindowFocusChanged(false)`)
и затем получает его обратно.

```
10-12 17:23:44.328 D/Lifecycle( 9579): StartActivity: onCreate
10-12 17:23:44.562 D/Lifecycle( 9579): StartActivity: onStart
10-12 17:23:44.563 D/Lifecycle( 9579): StartActivity: onResume
10-12 17:23:44.721 D/Lifecycle( 9579): StartActivity: onWindowFocusChanged(true)
10-12 17:23:46.006 D/Lifecycle( 9579): StartActivity: onWindowFocusChanged(false)
10-12 17:23:47.971 D/Lifecycle( 9579): StartActivity: onWindowFocusChanged(true)
```

## Сценарий 2. Переход через все 4 экрана и выход из приложения

Стартовый экран → регистрация (возврат результата) → вход → экран данных →
настройки → «назад» к данным → «назад» (выход).

StartActivity закрывается (`finish()`) после входа, поэтому из настроек по «назад»
виден только один предыдущий экран (ContentActivity), а следующий «назад» закрывает приложение.

```
10-12 17:23:50.759 D/Lifecycle( 9778): StartActivity: onCreate
10-12 17:23:50.988 D/Lifecycle( 9778): StartActivity: onStart
10-12 17:23:50.989 D/Lifecycle( 9778): StartActivity: onResume
10-12 17:23:51.174 D/Lifecycle( 9778): StartActivity: onWindowFocusChanged(true)
10-12 17:23:55.405 D/Lifecycle( 9778): StartActivity: onPause
10-12 17:23:55.413 D/Lifecycle( 9778): RegistrationActivity: onCreate
10-12 17:23:55.445 D/Lifecycle( 9778): RegistrationActivity: onStart
10-12 17:23:55.446 D/Lifecycle( 9778): RegistrationActivity: onResume
10-12 17:23:55.554 D/Lifecycle( 9778): StartActivity: onWindowFocusChanged(false)
10-12 17:23:55.620 D/Lifecycle( 9778): RegistrationActivity: onWindowFocusChanged(true)
10-12 17:23:56.021 D/Lifecycle( 9778): StartActivity: onStop
10-12 17:23:56.024 D/Lifecycle( 9778): StartActivity: onSaveInstanceState
10-12 17:24:16.251 D/Lifecycle( 9778): RegistrationActivity: onPause
10-12 17:24:16.256 D/Lifecycle( 9778): StartActivity: onRestart
10-12 17:24:16.257 D/Lifecycle( 9778): StartActivity: onStart
10-12 17:24:16.275 D/Lifecycle( 9778): StartActivity: onResume
10-12 17:24:16.354 D/Lifecycle( 9778): RegistrationActivity: onWindowFocusChanged(false)
10-12 17:24:16.388 D/Lifecycle( 9778): StartActivity: onWindowFocusChanged(true)
10-12 17:24:16.860 D/Lifecycle( 9778): RegistrationActivity: onStop
10-12 17:24:16.861 D/Lifecycle( 9778): RegistrationActivity: onDestroy
10-12 17:24:26.518 D/Lifecycle( 9778): StartActivity: onPause
10-12 17:24:26.528 D/Lifecycle( 9778): ContentActivity: onCreate
10-12 17:24:26.593 D/Lifecycle( 9778): ContentActivity: onStart
10-12 17:24:26.593 D/Lifecycle( 9778): ContentActivity: onResume
10-12 17:24:26.735 D/Lifecycle( 9778): StartActivity: onWindowFocusChanged(false)
10-12 17:24:26.821 D/Lifecycle( 9778): ContentActivity: onWindowFocusChanged(true)
10-12 17:24:27.223 D/Lifecycle( 9778): StartActivity: onStop
10-12 17:24:27.224 D/Lifecycle( 9778): StartActivity: onDestroy
10-12 17:24:31.868 D/Lifecycle( 9778): ContentActivity: onPause
10-12 17:24:31.879 D/Lifecycle( 9778): SettingsActivity: onCreate
10-12 17:24:31.994 D/Lifecycle( 9778): SettingsActivity: onStart
10-12 17:24:31.995 D/Lifecycle( 9778): SettingsActivity: onResume
10-12 17:24:32.368 D/Lifecycle( 9778): ContentActivity: onWindowFocusChanged(false)
10-12 17:24:32.473 D/Lifecycle( 9778): SettingsActivity: onWindowFocusChanged(true)
10-12 17:24:32.872 D/Lifecycle( 9778): ContentActivity: onStop
10-12 17:24:32.873 D/Lifecycle( 9778): ContentActivity: onSaveInstanceState
10-12 17:24:35.101 D/Lifecycle( 9778): SettingsActivity: onPause
10-12 17:24:35.106 D/Lifecycle( 9778): ContentActivity: onRestart
10-12 17:24:35.106 D/Lifecycle( 9778): ContentActivity: onStart
10-12 17:24:35.106 D/Lifecycle( 9778): ContentActivity: onResume
10-12 17:24:35.188 D/Lifecycle( 9778): SettingsActivity: onWindowFocusChanged(false)
10-12 17:24:35.218 D/Lifecycle( 9778): ContentActivity: onWindowFocusChanged(true)
10-12 17:24:35.693 D/Lifecycle( 9778): SettingsActivity: onStop
10-12 17:24:35.694 D/Lifecycle( 9778): SettingsActivity: onDestroy
10-12 17:24:37.850 D/Lifecycle( 9778): ContentActivity: onPause
10-12 17:24:37.894 D/Lifecycle( 9778): ContentActivity: onWindowFocusChanged(false)
10-12 17:24:38.369 D/Lifecycle( 9778): ContentActivity: onStop
10-12 17:24:38.370 D/Lifecycle( 9778): ContentActivity: onDestroy
```
