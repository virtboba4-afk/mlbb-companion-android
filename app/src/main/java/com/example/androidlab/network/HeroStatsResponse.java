package com.example.androidlab.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Ответ MLBBHub API на запрос GET /api/stats.
 */
public class HeroStatsResponse {

    @SerializedName("heroes")
    public List<HeroStatsDto> heroes;

    /** Общее количество героев на сервере (нужно, чтобы понять, есть ли следующая порция). */
    @SerializedName("total")
    public int total;

    /** false, если сервер сейчас не может отдать статистику. */
    @SerializedName("available")
    public boolean available;

    /** Дата обновления статистики в формате ISO-8601. */
    @SerializedName("lastUpdated")
    public String lastUpdated;
}
