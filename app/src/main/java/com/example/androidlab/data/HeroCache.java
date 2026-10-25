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
    /** Сколько всего героев доступно на сервере. */
    private static int total = 0;

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

    /** Добавляет очередную порцию с сервера. */
    public static void add(List<Hero> page, int serverTotal) {
        heroes.addAll(page);
        total = serverTotal;
    }

    /** Первая порция с сервера. */
    public static void replace(List<Hero> firstPage, int serverTotal) {
        heroes.clear();
        heroes.addAll(firstPage);
        fromBackup = false;
        total = serverTotal;
    }

    /** Данные из резервной копии: догружать с сервера нечего. */
    public static void replaceFromBackup(List<Hero> data) {
        heroes.clear();
        heroes.addAll(data);
        fromBackup = true;
        total = data.size();
    }

    public static int getTotal() {
        return total;
    }

    /** Есть ли на сервере ещё не загруженные герои. */
    public static boolean hasMore() {
        return !fromBackup && heroes.size() < total;
    }

    /** true, если текущие данные развёрнуты из резервной копии. */
    public static boolean isFromBackup() {
        return fromBackup;
    }

    public static void clear() {
        heroes.clear();
        fromBackup = false;
        total = 0;
    }
}
