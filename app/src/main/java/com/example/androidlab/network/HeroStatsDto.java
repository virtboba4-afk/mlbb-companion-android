package com.example.androidlab.network;

import com.google.gson.annotations.SerializedName;

/**
 * Статистика одного героя из ответа MLBBHub API (элемент массива "heroes").
 */
public class HeroStatsDto {

    /** Идентификатор героя в API, например "yi-sun-shin". */
    @SerializedName("slug")
    public String slug;

    /** Процент побед, %. */
    @SerializedName("winRate")
    public double winRate;

    /** Доля выборов героя среди всех выборов, %. */
    @SerializedName("pickRate")
    public double pickRate;

    /** Процент матчей, в которых героя запретили, %. */
    @SerializedName("banRate")
    public double banRate;

    /** Тир героя в текущей мете: S, A, B, C или D. */
    @SerializedName("tier")
    public String tier;

    /** Изменение процента побед за период, п.п. */
    @SerializedName("winRateDelta")
    public double winRateDelta;
}
