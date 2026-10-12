package com.example.androidlab;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidlab.model.Hero;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Список карточек героев + последний элемент с кнопкой "Загрузить ещё".
 */
public class HeroAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HERO = 0;
    private static final int TYPE_FOOTER = 1;

    private final Runnable onLoadMore;
    private List<Hero> heroes = new ArrayList<>();
    private boolean hasMore;

    public HeroAdapter(Runnable onLoadMore) {
        this.onLoadMore = onLoadMore;
    }

    public void submit(List<Hero> heroes, boolean hasMore) {
        this.heroes = new ArrayList<>(heroes);
        this.hasMore = hasMore;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return heroes.isEmpty() ? 0 : heroes.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position < heroes.size() ? TYPE_HERO : TYPE_FOOTER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HERO) {
            return new HeroHolder(inflater.inflate(R.layout.item_hero, parent, false));
        }
        return new FooterHolder(inflater.inflate(R.layout.item_load_more, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeroHolder) {
            ((HeroHolder) holder).bind(heroes.get(position));
        } else {
            ((FooterHolder) holder).bind(hasMore, onLoadMore);
        }
    }

    static class HeroHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final TextView tvAvatar;
        private final TextView tvName;
        private final TextView tvRole;
        private final TextView tvLane;
        private final TextView tvDifficulty;
        private final LinearProgressIndicator pbDifficulty;
        private final TextView tvWinRate;

        HeroHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardHero);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvLane = itemView.findViewById(R.id.tvLane);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            pbDifficulty = itemView.findViewById(R.id.pbDifficulty);
            tvWinRate = itemView.findViewById(R.id.tvWinRate);
        }

        void bind(Hero hero) {
            Context context = itemView.getContext();
            int roleColor = ContextCompat.getColor(context, hero.getRole().color);
            ColorStateList roleTint = ColorStateList.valueOf(roleColor);

            // Оформление карточки зависит от роли героя
            card.setStrokeColor(roleTint);
            tvAvatar.setBackgroundTintList(roleTint);
            tvRole.setBackgroundTintList(roleTint);
            pbDifficulty.setIndicatorColor(roleColor);

            tvAvatar.setText(hero.getName().substring(0, 1));
            tvName.setText(hero.getName());
            tvRole.setText(hero.getRole().title);
            tvLane.setText(hero.getLane().title);
            tvDifficulty.setText(context.getString(R.string.hero_difficulty, hero.getDifficulty()));
            pbDifficulty.setProgress(hero.getDifficulty());

            tvWinRate.setText(String.format(Locale.getDefault(), "%.1f%%", hero.getWinRate()));
            tvWinRate.setTextColor(ContextCompat.getColor(context,
                    hero.getWinRate() >= 50 ? R.color.win_rate_high : R.color.win_rate_low));
        }
    }

    static class FooterHolder extends RecyclerView.ViewHolder {

        private final MaterialButton btnLoadMore;
        private final TextView tvAllLoaded;

        FooterHolder(@NonNull View itemView) {
            super(itemView);
            btnLoadMore = itemView.findViewById(R.id.btnLoadMore);
            tvAllLoaded = itemView.findViewById(R.id.tvAllLoaded);
        }

        void bind(boolean hasMore, Runnable onLoadMore) {
            btnLoadMore.setVisibility(hasMore ? View.VISIBLE : View.GONE);
            tvAllLoaded.setVisibility(hasMore ? View.GONE : View.VISIBLE);
            btnLoadMore.setOnClickListener(v -> onLoadMore.run());
        }
    }
}
