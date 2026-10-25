package com.example.androidlab.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * REST API статистики героев Mobile Legends: https://mlbbhub.com/agents.md
 */
public interface MlbbHubApi {

    String BASE_URL = "https://mlbbhub.com/";

    /**
     * Статистика героев, отсортированная по полю sort.
     *
     * @param limit сколько героев вернуть (первые limit записей)
     * @param sort  поле сортировки: winRate, pickRate или banRate
     */
    @GET("api/stats")
    Call<HeroStatsResponse> getHeroStats(@Query("limit") int limit, @Query("sort") String sort);
}
