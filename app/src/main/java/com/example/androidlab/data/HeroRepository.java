package com.example.androidlab.data;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.androidlab.model.Hero;
import com.example.androidlab.model.Hero.Lane;
import com.example.androidlab.model.Hero.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Источник данных о героях. Отдаёт данные порционно (offset + limit), как REST API.
 * В лабораторной работе 3 заглушка заменяется на сетевой запрос.
 */
public class HeroRepository {

    public static final int PAGE_SIZE = 15;

    private static final List<Hero> ALL_HEROES = Arrays.asList(
            new Hero(1, "Miya", Role.MARKSMAN, Lane.GOLD, 2, 51.3),
            new Hero(2, "Balmond", Role.FIGHTER, Lane.JUNGLE, 2, 52.1),
            new Hero(3, "Saber", Role.ASSASSIN, Lane.JUNGLE, 3, 50.4),
            new Hero(4, "Alice", Role.MAGE, Lane.EXP, 5, 49.8),
            new Hero(5, "Nana", Role.MAGE, Lane.MID, 2, 52.6),
            new Hero(6, "Tigreal", Role.TANK, Lane.ROAM, 3, 51.9),
            new Hero(7, "Alucard", Role.FIGHTER, Lane.EXP, 3, 49.2),
            new Hero(8, "Karina", Role.ASSASSIN, Lane.JUNGLE, 4, 50.7),
            new Hero(9, "Akai", Role.TANK, Lane.ROAM, 4, 50.1),
            new Hero(10, "Franco", Role.TANK, Lane.ROAM, 6, 48.9),
            new Hero(11, "Bane", Role.FIGHTER, Lane.EXP, 3, 50.5),
            new Hero(12, "Bruno", Role.MARKSMAN, Lane.GOLD, 4, 49.6),
            new Hero(13, "Clint", Role.MARKSMAN, Lane.GOLD, 5, 50.0),
            new Hero(14, "Rafaela", Role.SUPPORT, Lane.ROAM, 2, 53.2),
            new Hero(15, "Eudora", Role.MAGE, Lane.MID, 2, 51.4),
            new Hero(16, "Zilong", Role.FIGHTER, Lane.EXP, 3, 50.9),
            new Hero(17, "Fanny", Role.ASSASSIN, Lane.JUNGLE, 10, 47.5),
            new Hero(18, "Layla", Role.MARKSMAN, Lane.GOLD, 1, 51.8),
            new Hero(19, "Estes", Role.SUPPORT, Lane.ROAM, 2, 53.0),
            new Hero(20, "Gord", Role.MAGE, Lane.MID, 3, 50.2),
            new Hero(21, "Hayabusa", Role.ASSASSIN, Lane.JUNGLE, 8, 49.0),
            new Hero(22, "Lolita", Role.TANK, Lane.ROAM, 4, 50.6),
            new Hero(23, "Natalia", Role.ASSASSIN, Lane.ROAM, 5, 49.7),
            new Hero(24, "Kagura", Role.MAGE, Lane.MID, 9, 48.1),
            new Hero(25, "Chou", Role.FIGHTER, Lane.EXP, 8, 49.4),
            new Hero(26, "Angela", Role.SUPPORT, Lane.ROAM, 3, 52.3),
            new Hero(27, "Johnson", Role.TANK, Lane.ROAM, 6, 50.8),
            new Hero(28, "Lesley", Role.MARKSMAN, Lane.GOLD, 5, 49.3),
            new Hero(29, "Gusion", Role.ASSASSIN, Lane.JUNGLE, 9, 48.6),
            new Hero(30, "Diggie", Role.SUPPORT, Lane.ROAM, 4, 51.1),
            new Hero(31, "Granger", Role.MARKSMAN, Lane.GOLD, 6, 49.9),
            new Hero(32, "Khufra", Role.TANK, Lane.ROAM, 5, 50.3),
            new Hero(33, "Lunox", Role.MAGE, Lane.MID, 7, 48.8),
            new Hero(34, "Ling", Role.ASSASSIN, Lane.JUNGLE, 10, 47.9),
            new Hero(35, "Floryn", Role.SUPPORT, Lane.ROAM, 2, 52.8),
            new Hero(36, "Fredrinn", Role.TANK, Lane.EXP, 6, 51.0)
    );

    private final AppPrefs prefs;

    public HeroRepository(Context context) {
        prefs = new AppPrefs(context);
    }

    /**
     * Загружает порцию героев.
     *
     * @return список (пустой, если данные закончились) или null, если нет соединения
     */
    @Nullable
    public List<Hero> loadPage(int offset, int limit) {
        if (prefs.isOfflineMode()) {
            return null;
        }
        if (offset >= ALL_HEROES.size()) {
            return new ArrayList<>();
        }
        int end = Math.min(offset + limit, ALL_HEROES.size());
        return new ArrayList<>(ALL_HEROES.subList(offset, end));
    }

    public boolean hasMore(int loadedCount) {
        return loadedCount < ALL_HEROES.size();
    }
}
