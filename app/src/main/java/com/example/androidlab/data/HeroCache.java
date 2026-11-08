package com.example.androidlab.data;

import com.example.androidlab.model.Hero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HeroCache {

    private static final List<Hero> heroes = new ArrayList<>();
    private static boolean fromBackup = false;
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

    public static void add(List<Hero> page, int serverTotal) {
        heroes.addAll(page);
        total = serverTotal;
    }

    public static void replace(List<Hero> firstPage, int serverTotal) {
        heroes.clear();
        heroes.addAll(firstPage);
        fromBackup = false;
        total = serverTotal;
    }

    public static void replaceFromBackup(List<Hero> data) {
        heroes.clear();
        heroes.addAll(data);
        fromBackup = true;
        total = data.size();
    }

    public static int getTotal() {
        return total;
    }

    public static boolean hasMore() {
        return !fromBackup && heroes.size() < total;
    }

    public static boolean isFromBackup() {
        return fromBackup;
    }

    public static void clear() {
        heroes.clear();
        fromBackup = false;
        total = 0;
    }
}
