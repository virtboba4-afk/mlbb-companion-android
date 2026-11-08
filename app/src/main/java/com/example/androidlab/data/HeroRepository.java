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

public class HeroRepository {

    public static final int PAGE_SIZE = 15;

    private static final String SORT = "winRate";

    public enum ErrorType {
        NO_CONNECTION,
        NETWORK,
        HTTP,
        BAD_RESPONSE,
        EMPTY
    }

    public interface PageCallback {
        void onPage(@NonNull List<Hero> page, int total);

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

    public boolean isOnline() {
        if (prefs.isOfflineMode() || connectivityManager == null) {
            return false;
        }
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    @Nullable
    public Call<HeroStatsResponse> loadPage(int offset, int limit, @NonNull PageCallback callback) {
        if (!isOnline()) {
            callback.onError(ErrorType.NO_CONNECTION, 0);
            return null;
        }

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
                    return;
                }
                if (t instanceof IOException) {
                    callback.onError(isOnline() ? ErrorType.NETWORK : ErrorType.NO_CONNECTION, 0);
                } else {
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
