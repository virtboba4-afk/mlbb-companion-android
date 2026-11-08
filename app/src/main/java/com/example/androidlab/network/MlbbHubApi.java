package com.example.androidlab.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MlbbHubApi {

    String BASE_URL = "https://mlbbhub.com/";

    @GET("api/stats")
    Call<HeroStatsResponse> getHeroStats(@Query("limit") int limit, @Query("sort") String sort);
}
