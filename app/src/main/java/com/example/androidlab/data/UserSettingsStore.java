package com.example.androidlab.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.datastore.preferences.SharedPreferencesMigrationKt;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public final class UserSettingsStore {

    private static final String STORE_NAME = "user_settings";

    private static final Preferences.Key<String> KEY_FULL_NAME = PreferencesKeys.stringKey("full_name");
    private static final Preferences.Key<String> KEY_GENDER = PreferencesKeys.stringKey("gender");
    private static final Preferences.Key<Integer> KEY_AGE = PreferencesKeys.intKey("age");
    private static final Preferences.Key<String> KEY_FILE_NAME = PreferencesKeys.stringKey("file_name");

    public static final String GENDER_NONE = "";
    public static final String GENDER_MALE = "male";
    public static final String GENDER_FEMALE = "female";

    private static final String LIST_NUMBER = "00";

    public static final String DEFAULT_FILE_NAME = "heroes_" + LIST_NUMBER;

    public static final class Profile {
        public final String fullName;
        public final String gender;
        public final int age;

        Profile(String fullName, String gender, int age) {
            this.fullName = fullName;
            this.gender = gender;
            this.age = age;
        }
    }

    private static UserSettingsStore instance;

    private final RxDataStore<Preferences> dataStore;

    public static synchronized UserSettingsStore get(Context context) {
        if (instance == null) {
            instance = new UserSettingsStore(context.getApplicationContext());
        }
        return instance;
    }

    private UserSettingsStore(Context context) {
        Set<String> migratedKeys = new HashSet<>(Arrays.asList(
                KEY_FULL_NAME.getName(), KEY_GENDER.getName(), KEY_AGE.getName()));
        dataStore = new RxPreferenceDataStoreBuilder(context, STORE_NAME)
                .addDataMigration(SharedPreferencesMigrationKt.SharedPreferencesMigration(
                        context, AppPrefs.FILE, migratedKeys))
                .build();
    }

    @NonNull
    public Single<Profile> getProfile() {
        return dataStore.data().firstOrError().map(data -> {
            String fullName = data.get(KEY_FULL_NAME);
            String gender = data.get(KEY_GENDER);
            Integer age = data.get(KEY_AGE);
            return new Profile(fullName != null ? fullName : "",
                    gender != null ? gender : GENDER_NONE,
                    age != null ? age : 0);
        });
    }

    @NonNull
    public Completable saveProfile(String fullName, String gender, int age) {
        return dataStore.updateDataAsync(data -> {
            MutablePreferences updated = data.toMutablePreferences();
            updated.set(KEY_FULL_NAME, fullName);
            updated.set(KEY_GENDER, gender);
            updated.set(KEY_AGE, age);
            return Single.just(updated);
        }).ignoreElement();
    }

    @NonNull
    public Single<String> getFileName() {
        return dataStore.data().firstOrError().map(data -> {
            String name = data.get(KEY_FILE_NAME);
            return name != null ? name : DEFAULT_FILE_NAME;
        });
    }

    @NonNull
    public Completable setFileName(String name) {
        return dataStore.updateDataAsync(data -> {
            MutablePreferences updated = data.toMutablePreferences();
            updated.set(KEY_FILE_NAME, name);
            return Single.just(updated);
        }).ignoreElement();
    }
}
