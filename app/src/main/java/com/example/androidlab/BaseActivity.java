package com.example.androidlab;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidlab.data.AppPrefs;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String LOG_TAG = "Lifecycle";

    private String appliedAppearance;

    protected void log(String method) {
        Log.d(LOG_TAG, getClass().getSimpleName() + ": " + method);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Configuration config = new Configuration(newBase.getResources().getConfiguration());
        config.fontScale *= new AppPrefs(newBase).getFontScale();
        super.attachBaseContext(newBase.createConfigurationContext(config));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppPrefs prefs = new AppPrefs(this);
        setTheme(prefs.getPaletteTheme());
        appliedAppearance = appearanceOf(prefs);

        super.onCreate(savedInstanceState);
        log("onCreate");
    }

    private static String appearanceOf(AppPrefs prefs) {
        return prefs.getPalette() + "|" + prefs.getFontScaleIndex();
    }

    public void recreateIfAppearanceChanged() {
        if (!appearanceOf(new AppPrefs(this)).equals(appliedAppearance)) {
            recreate();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        log("onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        log("onRestart");
        recreateIfAppearanceChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        log("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        log("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("onDestroy");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        log("onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        log("onRestoreInstanceState");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        log("onWindowFocusChanged(" + hasFocus + ")");
    }
}
