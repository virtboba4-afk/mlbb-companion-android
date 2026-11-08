package com.example.androidlab.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.androidlab.model.Hero;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HeroesFileStorage {

    private static final String EXTENSION = ".txt";
    private static final String BACKUP_FILE_NAME = "heroes_file_backup.txt";

    public static final class State {
        public final String fileName;
        public final String filePath;
        public final boolean fileExists;
        public final long fileSize;
        public final long fileTime;
        public final boolean backupExists;
        public final long backupSize;
        public final long backupTime;

        State(File file, File backup) {
            fileName = file.getName();
            filePath = file.getParent();
            fileExists = file.exists();
            fileSize = file.length();
            fileTime = file.lastModified();
            backupExists = backup.exists();
            backupSize = backup.length();
            backupTime = backup.lastModified();
        }
    }

    private final Context context;
    private final UserSettingsStore settings;
    private final File publicDir;
    private final File backupFile;

    public HeroesFileStorage(Context context) {
        this.context = context.getApplicationContext();
        settings = UserSettingsStore.get(context);
        publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        backupFile = new File(context.getFilesDir(), BACKUP_FILE_NAME);
    }

    public boolean needsPermission() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    public Single<State> getState() {
        return settings.getFileName()
                .observeOn(Schedulers.single())
                .map(name -> new State(fileFor(name), backupFile))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    public Completable export(List<Hero> heroes) {
        List<Hero> snapshot = new ArrayList<>(heroes);
        return withFile(file -> {
            publicDir.mkdirs();
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(format(snapshot).getBytes(StandardCharsets.UTF_8));
            }
        });
    }

    @NonNull
    public Completable delete() {
        return withFile(file -> {
            copy(file, backupFile);
            if (!file.delete()) {
                throw new IOException("Не удалось удалить " + file);
            }
        });
    }

    @NonNull
    public Completable restore() {
        return withFile(file -> {
            publicDir.mkdirs();
            copy(backupFile, file);
            backupFile.delete();
        });
    }

    @NonNull
    public Completable rename(String newName) {
        return settings.getFileName()
                .observeOn(Schedulers.single())
                .flatMapCompletable(oldName -> {
                    File from = fileFor(oldName);
                    File to = fileFor(newName);
                    if (!oldName.equals(newName) && from.exists()
                            && (to.exists() || !from.renameTo(to))) {
                        return Completable.error(new IOException("Не удалось переименовать " + from));
                    }
                    return settings.setFileName(newName);
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Completable withFile(Consumer<File> action) {
        return settings.getFileName()
                .observeOn(Schedulers.single())
                .flatMapCompletable(name -> Completable.fromAction(() -> action.accept(fileFor(name))))
                .observeOn(AndroidSchedulers.mainThread());
    }

    private File fileFor(String name) {
        return new File(publicDir, name + EXTENSION);
    }

    private static String format(List<Hero> heroes) {
        StringBuilder text = new StringBuilder("ML Guide: статистика героев\n")
                .append("Героев в файле: ").append(heroes.size()).append("\n\n");
        int number = 1;
        for (Hero hero : heroes) {
            text.append(String.format(Locale.US,
                    "%d. %s | Тир %s | Побед %.2f%% | Выбор %.2f%% | Бан %.2f%% | За неделю %+.2f п.п.\n",
                    number++, hero.getName(), hero.getTier().name(), hero.getWinRate(),
                    hero.getPickRate(), hero.getBanRate(), hero.getWinRateDelta()));
        }
        return text.toString();
    }

    private static void copy(File from, File to) throws IOException {
        try (FileInputStream in = new FileInputStream(from);
             FileOutputStream out = new FileOutputStream(to)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
        }
    }
}
