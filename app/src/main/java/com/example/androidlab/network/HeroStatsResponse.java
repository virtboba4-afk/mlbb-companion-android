package com.example.androidlab.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HeroStatsResponse {

    @SerializedName("heroes")
    public List<HeroStatsDto> heroes;

    @SerializedName("total")
    public int total;

    @SerializedName("available")
    public boolean available;

    @SerializedName("lastUpdated")
    public String lastUpdated;
}
