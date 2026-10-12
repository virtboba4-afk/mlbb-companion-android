package com.example.androidlab.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Аватар пользователя: копия выбранного изображения во внутреннем хранилище.
 */
public class AvatarStorage {

    private static final String FILE_NAME = "avatar.jpg";

    private final Context context;
    private final File file;

    public AvatarStorage(Context context) {
        this.context = context.getApplicationContext();
        file = new File(this.context.getFilesDir(), FILE_NAME);
    }

    public boolean save(Uri source) {
        try (InputStream in = context.getContentResolver().openInputStream(source);
             OutputStream out = new FileOutputStream(file)) {
            if (in == null) {
                return false;
            }
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Загружает уменьшенную копию аватара (не больше maxSize по стороне) или null. */
    @Nullable
    public Bitmap load(int maxSize) {
        if (!file.exists()) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), bounds);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        while (bounds.outWidth / (options.inSampleSize * 2) >= maxSize
                && bounds.outHeight / (options.inSampleSize * 2) >= maxSize) {
            options.inSampleSize *= 2;
        }
        return BitmapFactory.decodeFile(file.getPath(), options);
    }
}
