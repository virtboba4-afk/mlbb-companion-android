package com.example.androidlab;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.androidlab.data.AppPrefs;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Светлая / тёмная тема, выбранная пользователем в настройках
        AppCompatDelegate.setDefaultNightMode(new AppPrefs(this).getThemeMode());
    }
}
