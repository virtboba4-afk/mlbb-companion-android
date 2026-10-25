package com.example.androidlab.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.androidlab.model.Hero;
import com.example.androidlab.network.ApiClient;
import com.example.androidlab.network.HeroStatsDto;
import com.example.androidlab.network.HeroStatsResponse;
import com.example.androidlab.network.MlbbHubApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Источник данных о героях: статистика рейтинговых матчей с MLBBHub API.
 * Данные запрашиваются порциями по {@link #PAGE_SIZE} героев.
 */
public class HeroRepository {

    public static final int PAGE_SIZE = 15;

    /** Сортировка списка: сильнейшие по проценту побед герои первыми. */
    private static final String SORT = "winRate";

    public enum ErrorType {
        /** Нет подключения к интернету (или включён офлайн-режим). */
        NO_CONNECTION,
        /** Подключение есть, но сервер не ответил (таймаут, DNS и т.п.). */
        NETWORK,
        /** Сервер ответил кодом ошибки HTTP. */
        HTTP,
        /** Ответ не удалось разобрать или сервер сообщил, что статистика недоступна. */
        BAD_RESPONSE,
        /** Сервер вернул пустой список. */
        EMPTY
    }

    public interface PageCallback {
        /**
         * @param page  очередная порция героев (пустая, если загружено всё)
         * @param total общее количество героев на сервере
         */
        void onPage(@NonNull List<Hero> page, int total);

        /** @param httpCode код ответа для {@link ErrorType#HTTP}, иначе 0 */
        void onError(@NonNull ErrorType type, int httpCode);
    }

    private final AppPrefs prefs;
    private final ConnectivityManager connectivityManager;
    private final MlbbHubApi api;

    public HeroRepository(Context context) {
        prefs = new AppPrefs(context);
        connectivityManager = context.getSystemService(ConnectivityManager.class);
        api = ApiClient.getApi();
    }

    /** Есть ли сейчас подключение к интернету (с учётом имитации офлайн-режима из настроек). */
    public boolean isOnline() {
        if (prefs.isOfflineMode() || connectivityManager == null) {
            return false;
        }
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /**
     * Асинхронно загружает героев с offset по offset + limit. Результат приходит в главном потоке.
     *
     * @return запрос, который можно отменить, или null, если запрос не отправлялся
     */
    @Nullable
    public Call<HeroStatsResponse> loadPage(int offset, int limit, @NonNull PageCallback callback) {
        if (!isOnline()) {
            callback.onError(ErrorType.NO_CONNECTION, 0);
            return null;
        }

        // API умеет только ограничивать количество записей (limit), поэтому следующая
        // порция - это первые offset + limit героев, из которых берутся последние limit
        Call<HeroStatsResponse> call = api.getHeroStats(offset + limit, SORT);
        call.enqueue(new Callback<HeroStatsResponse>() {
            @Override
            public void onResponse(@NonNull Call<HeroStatsResponse> call,
                                   @NonNull Response<HeroStatsResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onError(ErrorType.HTTP, response.code());
                    return;
                }
                HeroStatsResponse body = response.body();
                if (body == null || !body.available || body.heroes == null) {
                    callback.onError(ErrorType.BAD_RESPONSE, 0);
                    return;
                }

                List<Hero> page = new ArrayList<>();
                for (int i = offset; i < body.heroes.size(); i++) {
                    Hero hero = toHero(body.heroes.get(i));
                    if (hero != null) {
                        page.add(hero);
                    }
                }
                if (offset == 0 && page.isEmpty()) {
                    callback.onError(ErrorType.EMPTY, 0);
                    return;
                }
                callback.onPage(page, Math.max(body.total, offset + page.size()));
            }

            @Override
            public void onFailure(@NonNull Call<HeroStatsResponse> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    // Экран закрыт - результат никому не нужен
                    return;
                }
                if (t instanceof IOException) {
                    callback.onError(isOnline() ? ErrorType.NETWORK : ErrorType.NO_CONNECTION, 0);
                } else {
                    // Например, JsonSyntaxException: сервер прислал не то, что ожидалось
                    callback.onError(ErrorType.BAD_RESPONSE, 0);
                }
            }
        });
        return call;
    }

    @Nullable
    private static Hero toHero(HeroStatsDto dto) {
        if (dto == null || dto.slug == null || dto.slug.trim().isEmpty()) {
            return null;
        }
        return new Hero(dto.slug.trim(), Hero.Tier.parse(dto.tier),
                dto.winRate, dto.pickRate, dto.banRate, dto.winRateDelta);
    }
}
