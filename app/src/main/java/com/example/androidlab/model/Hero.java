package com.example.androidlab.model;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import com.example.androidlab.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Герой Mobile Legends: Bang Bang - объект предметной области.
 */
public class Hero {

    public enum Role {
        TANK(R.string.role_tank, R.color.role_tank),
        FIGHTER(R.string.role_fighter, R.color.role_fighter),
        ASSASSIN(R.string.role_assassin, R.color.role_assassin),
        MAGE(R.string.role_mage, R.color.role_mage),
        MARKSMAN(R.string.role_marksman, R.color.role_marksman),
        SUPPORT(R.string.role_support, R.color.role_support);

        @StringRes public final int title;
        @ColorRes public final int color;

        Role(@StringRes int title, @ColorRes int color) {
            this.title = title;
            this.color = color;
        }
    }

    public enum Lane {
        GOLD(R.string.lane_gold),
        EXP(R.string.lane_exp),
        MID(R.string.lane_mid),
        JUNGLE(R.string.lane_jungle),
        ROAM(R.string.lane_roam);

        @StringRes public final int title;

        Lane(@StringRes int title) {
            this.title = title;
        }
    }

    private final int id;
    private final String name;
    private final Role role;
    private final Lane lane;
    private final int difficulty;
    private final double winRate;

    public Hero(int id, String name, Role role, Lane lane, int difficulty, double winRate) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.lane = lane;
        this.difficulty = difficulty;
        this.winRate = winRate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public Lane getLane() {
        return lane;
    }

    /** Сложность освоения героя по шкале 1..10. */
    public int getDifficulty() {
        return difficulty;
    }

    /** Процент побед, %. */
    public double getWinRate() {
        return winRate;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("role", role.name());
        json.put("lane", lane.name());
        json.put("difficulty", difficulty);
        json.put("winRate", winRate);
        return json;
    }

    public static Hero fromJson(JSONObject json) throws JSONException {
        return new Hero(
                json.getInt("id"),
                json.getString("name"),
                Role.valueOf(json.getString("role")),
                Lane.valueOf(json.getString("lane")),
                json.getInt("difficulty"),
                json.getDouble("winRate")
        );
    }
}
