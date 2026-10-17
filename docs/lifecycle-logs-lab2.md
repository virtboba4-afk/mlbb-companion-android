# Лабораторная 2: логи жизненного цикла фрагментов

Все экраны - фрагменты внутри единственной `MainActivity` (Navigation Component, граф `res/navigation/nav_graph.xml`).
Фрагменты логируют методы жизненного цикла через `BaseFragment`, деятельность - через `BaseActivity`.

Фильтр в LogCat: `tag:Lifecycle` (формат `[название класса]: [название метода]`).

## Сценарий: стартовое окно → регистрация → данные → настройки → выход из приложения

Действия (эмулятор Pixel 9a, Android 17, первый запуск):

1. Запуск приложения - `StartFragment`.
2. «Зарегистрироваться» - `RegistrationFragment` (логин со стартового экрана передан через Safe Args).
3. Заполнение формы, «Зарегистрироваться» - результат через Fragment Result API, возврат на `StartFragment`.
4. Ввод пароля, «Войти» - `ContentFragment` (`StartFragment` удаляется из стека: `popUpTo` + `popUpToInclusive`).
5. Шестерёнка в тулбаре - `SettingsFragment`.
6. «Назад» - возврат на `ContentFragment`.
7. «Назад» - выход из приложения (стартового экрана в стеке уже нет).

```
MainActivity: onCreate
StartFragment: onAttach
StartFragment: onCreate
StartFragment: onCreateView
StartFragment: onViewCreated
StartFragment: onViewStateRestored
StartFragment: onStart
MainActivity: onStart
MainActivity: onResume
StartFragment: onResume
MainActivity: onWindowFocusChanged(true)
StartFragment: onPause
StartFragment: onStop
RegistrationFragment: onAttach
RegistrationFragment: onCreate
RegistrationFragment: onCreateView
RegistrationFragment: onViewCreated
RegistrationFragment: onViewStateRestored
RegistrationFragment: onStart
RegistrationFragment: onResume
StartFragment: onDestroyView
RegistrationFragment: onPause
RegistrationFragment: onStop
StartFragment: onCreateView
StartFragment: onViewCreated
StartFragment: onViewStateRestored
StartFragment: onStart
StartFragment: onResume
RegistrationFragment: onDestroyView
RegistrationFragment: onDestroy
RegistrationFragment: onDetach
StartFragment: onPause
StartFragment: onStop
ContentFragment: onAttach
ContentFragment: onCreate
ContentFragment: onCreateView
ContentFragment: onViewCreated
ContentFragment: onViewStateRestored
ContentFragment: onStart
ContentFragment: onResume
StartFragment: onDestroyView
StartFragment: onDestroy
StartFragment: onDetach
ContentFragment: onPause
ContentFragment: onStop
SettingsFragment: onAttach
SettingsFragment: onCreate
SettingsFragment: onCreateView
SettingsFragment: onViewCreated
SettingsFragment: onViewStateRestored
SettingsFragment: onStart
SettingsFragment: onResume
ContentFragment: onDestroyView
SettingsFragment: onPause
SettingsFragment: onStop
ContentFragment: onCreateView
ContentFragment: onViewCreated
ContentFragment: onViewStateRestored
ContentFragment: onStart
ContentFragment: onResume
SettingsFragment: onDestroyView
SettingsFragment: onDestroy
SettingsFragment: onDetach
ContentFragment: onPause
MainActivity: onPause
MainActivity: onWindowFocusChanged(false)
ContentFragment: onStop
MainActivity: onStop
ContentFragment: onDestroyView
ContentFragment: onDestroy
ContentFragment: onDetach
MainActivity: onDestroy
```

## Пояснения

- Фрагмент, уходящий в стек (`StartFragment` при открытии регистрации, `ContentFragment` при открытии настроек),
  проходит `onPause → onStop → onDestroyView`, но не `onDestroy`: объект фрагмента и его состояние сохраняются,
  уничтожается только разметка. При возврате он снова начинает с `onCreateView`.
- Закрываемый фрагмент (`RegistrationFragment` после регистрации, `SettingsFragment` по «назад») проходит
  полный путь до `onDestroy → onDetach`.
- После входа `StartFragment` получает `onDestroy → onDetach`, потому что действие `action_start_to_content`
  убирает его из стека. Поэтому из настроек по «назад» виден только один предыдущий экран, а со
  следующего «назад» приложение закрывается.
- Методы `MainActivity` вызываются только при запуске и выходе из приложения: смена экранов
  внутри деятельности её жизненный цикл не затрагивает.
