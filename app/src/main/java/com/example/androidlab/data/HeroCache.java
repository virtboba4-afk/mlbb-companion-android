package com.example.androidlab.data;

import com.example.androidlab.model.Hero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Данные, загруженные в текущей сессии. Общие для экрана данных и экрана настроек
 * (оттуда создаётся резервная копия и разворачиваются данные из неё).
 */
public final class HeroCache {

    private static final List<Hero> heroes = new ArrayList<>();
    private static boolean fromBackup = false;

    private HeroCache() {
    }

    public static List<Hero> get() {
        return Collections.unmodifiableList(heroes);
    }

    public static boolean isEmpty() {
        return heroes.isEmpty();
    }

    public static int size() {
        return heroes.size();
    }

    public static void add(List<Hero> page) {
        heroes.addAll(page);
    }

    public static void replace(List<Hero> data, boolean restoredFromBackup) {
        heroes.clear();
        heroes.addAll(data);
        fromBackup = restoredFromBackup;
    }

    /** true, если текущие данные развёрнуты из резервной копии. */
    public static boolean isFromBackup() {
        return fromBackup;
    }

    public static void clear() {
        heroes.clear();
        fromBackup = false;
    }
}
