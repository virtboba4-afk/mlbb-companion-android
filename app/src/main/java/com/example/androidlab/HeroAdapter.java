package com.example.androidlab;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidlab.databinding.ItemHeroBinding;
import com.example.androidlab.databinding.ItemLoadMoreBinding;
import com.example.androidlab.model.Hero;

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
            return new HeroHolder(ItemHeroBinding.inflate(inflater, parent, false));
        }
        return new FooterHolder(ItemLoadMoreBinding.inflate(inflater, parent, false));
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

        private final ItemHeroBinding binding;

        HeroHolder(@NonNull ItemHeroBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Hero hero) {
            Context context = itemView.getContext();
            int roleColor = ContextCompat.getColor(context, hero.getRole().color);
            ColorStateList roleTint = ColorStateList.valueOf(roleColor);

            // Оформление карточки зависит от роли героя
            binding.cardHero.setStrokeColor(roleTint);
            binding.tvAvatar.setBackgroundTintList(roleTint);
            binding.tvRole.setBackgroundTintList(roleTint);
            binding.pbDifficulty.setIndicatorColor(roleColor);

            binding.tvAvatar.setText(hero.getName().substring(0, 1));
            binding.tvName.setText(hero.getName());
            binding.tvRole.setText(hero.getRole().title);
            binding.tvLane.setText(hero.getLane().title);
            binding.tvDifficulty.setText(context.getString(R.string.hero_difficulty, hero.getDifficulty()));
            binding.pbDifficulty.setProgress(hero.getDifficulty());

            binding.tvWinRate.setText(String.format(Locale.getDefault(), "%.1f%%", hero.getWinRate()));
            binding.tvWinRate.setTextColor(ContextCompat.getColor(context,
                    hero.getWinRate() >= 50 ? R.color.win_rate_high : R.color.win_rate_low));
        }
    }

    static class FooterHolder extends RecyclerView.ViewHolder {

        private final ItemLoadMoreBinding binding;

        FooterHolder(@NonNull ItemLoadMoreBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(boolean hasMore, Runnable onLoadMore) {
            binding.btnLoadMore.setVisibility(hasMore ? View.VISIBLE : View.GONE);
            binding.tvAllLoaded.setVisibility(hasMore ? View.GONE : View.VISIBLE);
            binding.btnLoadMore.setOnClickListener(v -> onLoadMore.run());
        }
    }
}
