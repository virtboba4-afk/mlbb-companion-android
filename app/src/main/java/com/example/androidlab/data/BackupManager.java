package com.example.androidlab.data;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.androidlab.model.Hero;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Резервная копия списка героев во внутреннем хранилище приложения (JSON-файл).
 */
public class BackupManager {

    private static final String FILE_NAME = "heroes_backup.json";

    private final File file;

    public BackupManager(Context context) {
        file = new File(context.getFilesDir(), FILE_NAME);
    }

    public boolean exists() {
        return file.exists();
    }

    /** Время создания копии (мс) или 0, если копии нет. */
    public long getCreatedAt() {
        return file.exists() ? file.lastModified() : 0;
    }

    public int getCount() {
        List<Hero> heroes = load();
        return heroes == null ? 0 : heroes.size();
    }

    public boolean save(List<Hero> heroes) {
        try {
            JSONArray array = new JSONArray();
            for (Hero hero : heroes) {
                array.put(hero.toJson());
            }
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(array.toString().getBytes(StandardCharsets.UTF_8));
            }
            return true;
        } catch (JSONException | IOException e) {
            return false;
        }
    }

    /** Загружает героев из копии или возвращает null, если копии нет или она повреждена. */
    @Nullable
    public List<Hero> load() {
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            int read = 0;
            while (read < bytes.length) {
                int n = in.read(bytes, read, bytes.length - read);
                if (n < 0) break;
                read += n;
            }
            JSONArray array = new JSONArray(new String(bytes, 0, read, StandardCharsets.UTF_8));
            List<Hero> heroes = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                heroes.add(Hero.fromJson(array.getJSONObject(i)));
            }
            return heroes;
        } catch (JSONException | IOException e) {
            return null;
        }
    }

    public boolean delete() {
        return file.delete();
    }
}
