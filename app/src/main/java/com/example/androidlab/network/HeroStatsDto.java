package com.example.androidlab.network;

import com.google.gson.annotations.SerializedName;

public class HeroStatsDto {

    @SerializedName("slug")
    public String slug;

    @SerializedName("winRate")
    public double winRate;

    @SerializedName("pickRate")
    public double pickRate;

    @SerializedName("banRate")
    public double banRate;

    @SerializedName("tier")
    public String tier;

    @SerializedName("winRateDelta")
    public double winRateDelta;
}
