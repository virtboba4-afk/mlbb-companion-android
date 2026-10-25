package com.example.androidlab.model;

import androidx.annotation.ColorRes;

import com.example.androidlab.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Герой Mobile Legends: Bang Bang и его статистика в рейтинговых матчах - объект предметной области.
 */
public class Hero {

    /** Тир героя в текущей мете - категория, от которой зависит оформление карточки. */
    public enum Tier {
        S(R.color.tier_s),
        A(R.color.tier_a),
        B(R.color.tier_b),
        C(R.color.tier_c),
        D(R.color.tier_d);

        @ColorRes public final int color;

        Tier(@ColorRes int color) {
            this.color = color;
        }

        /** Тир из строки API; неизвестное значение считается самым низким тиром. */
        public static Tier parse(String value) {
            if (value != null) {
                for (Tier tier : values()) {
                    if (tier.name().equalsIgnoreCase(value.trim())) {
                        return tier;
                    }
                }
            }
            return D;
        }
    }

    private final String slug;
    private final String name;
    private final Tier tier;
    private final double winRate;
    private final double pickRate;
    private final double banRate;
    private final double winRateDelta;

    public Hero(String slug, Tier tier, double winRate, double pickRate, double banRate,
                double winRateDelta) {
        this.slug = slug;
        this.name = nameFromSlug(slug);
        this.tier = tier;
        this.winRate = winRate;
        this.pickRate = pickRate;
        this.banRate = banRate;
        this.winRateDelta = winRateDelta;
    }

    /** "yi-sun-shin" -> "Yi Sun Shin". */
    private static String nameFromSlug(String slug) {
        StringBuilder name = new StringBuilder();
        for (String part : slug.split("-")) {
            if (part.isEmpty()) continue;
            if (name.length() > 0) name.append(' ');
            name.append(part.substring(0, 1).toUpperCase(Locale.ROOT)).append(part.substring(1));
        }
        return name.toString();
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public Tier getTier() {
        return tier;
    }

    /** Процент побед, %. */
    public double getWinRate() {
        return winRate;
    }

    /** Доля выборов героя, %. */
    public double getPickRate() {
        return pickRate;
    }

    /** Процент матчей с запретом героя, %. */
    public double getBanRate() {
        return banRate;
    }

    /** Изменение процента побед за неделю, п.п. */
    public double getWinRateDelta() {
        return winRateDelta;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("slug", slug);
        json.put("tier", tier.name());
        json.put("winRate", winRate);
        json.put("pickRate", pickRate);
        json.put("banRate", banRate);
        json.put("winRateDelta", winRateDelta);
        return json;
    }

    public static Hero fromJson(JSONObject json) throws JSONException {
        return new Hero(
                json.getString("slug"),
                Tier.parse(json.getString("tier")),
                json.getDouble("winRate"),
                json.getDouble("pickRate"),
                json.getDouble("banRate"),
                json.getDouble("winRateDelta")
        );
    }
}
