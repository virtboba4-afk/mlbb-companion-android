package com.example.androidlab.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Хранилище данных пользователя и настроек приложения (SharedPreferences).
 */
public class AppPrefs {

    private static final String FILE = "ml_guide_prefs";

    private static final String KEY_LOGIN = "login";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_AGE = "age";

    private static final String KEY_OFFLINE = "offline_mode";

    public static final String GENDER_NONE = "";
    public static final String GENDER_MALE = "male";
    public static final String GENDER_FEMALE = "female";

    private final SharedPreferences prefs;

    public AppPrefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    // --- Пользователь ---

    public boolean hasUser() {
        return !getLogin().isEmpty();
    }

    public void saveUser(String login, String password, String nickname) {
        prefs.edit()
                .putString(KEY_LOGIN, login)
                .putString(KEY_PASSWORD, password)
                .putString(KEY_NICKNAME, nickname)
                .apply();
    }

    public boolean checkCredentials(String login, String password) {
        return login.equals(getLogin()) && password.equals(prefs.getString(KEY_PASSWORD, ""));
    }

    public void setPassword(String password) {
        prefs.edit().putString(KEY_PASSWORD, password).apply();
    }

    public String getLogin() {
        return prefs.getString(KEY_LOGIN, "");
    }

    public String getNickname() {
        return prefs.getString(KEY_NICKNAME, "");
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public String getGender() {
        return prefs.getString(KEY_GENDER, GENDER_NONE);
    }

    /** Возраст пользователя или 0, если не задан. */
    public int getAge() {
        return prefs.getInt(KEY_AGE, 0);
    }

    public void saveProfile(String nickname, String fullName, String gender, int age) {
        prefs.edit()
                .putString(KEY_NICKNAME, nickname)
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_GENDER, gender)
                .putInt(KEY_AGE, age)
                .apply();
    }

    // --- Настройки ---

    /** Имитация отсутствия интернета, чтобы проверить работу с резервной копией. */
    public boolean isOfflineMode() {
        return prefs.getBoolean(KEY_OFFLINE, false);
    }

    public void setOfflineMode(boolean offline) {
        prefs.edit().putBoolean(KEY_OFFLINE, offline).apply();
    }
}
