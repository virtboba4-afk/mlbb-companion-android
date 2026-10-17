package com.example.androidlab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.androidlab.data.BackupManager;
import com.example.androidlab.data.HeroCache;
import com.example.androidlab.data.HeroRepository;
import com.example.androidlab.databinding.FragmentContentBinding;
import com.example.androidlab.model.Hero;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ContentFragment extends BaseFragment {

    private static final String STATE_GREETED = "greeted";

    private HeroRepository repository;
    private BackupManager backupManager;
    private HeroAdapter adapter;

    private FragmentContentBinding binding;

    /** Приветствие показывается один раз после входа, а не при каждом возврате с настроек. */
    private boolean greeted;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = new HeroRepository(requireContext());
        backupManager = new BackupManager(requireContext());
        if (savedInstanceState != null) {
            greeted = savedInstanceState.getBoolean(STATE_GREETED);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentContentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                openSettings();
                return true;
            }
            return false;
        });

        adapter = new HeroAdapter(this::loadMore);
        binding.rvHeroes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHeroes.setAdapter(adapter);

        binding.btnRetry.setOnClickListener(v -> loadFirstPage());
        binding.btnUseBackup.setOnClickListener(v -> restoreFromBackup());

        if (HeroCache.isEmpty()) {
            loadFirstPage();
        }

        if (!greeted) {
            // Никнейм передан со стартового экрана через Safe Args
            String nickname = ContentFragmentArgs.fromBundle(requireArguments()).getNickname();
            Snackbar.make(binding.getRoot(), getString(R.string.content_greeting, nickname),
                    Snackbar.LENGTH_SHORT).show();
            greeted = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Данные могли измениться на экране настроек (восстановление из копии)
        render();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_GREETED, greeted);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadFirstPage() {
        List<Hero> page = repository.loadPage(0, HeroRepository.PAGE_SIZE);
        if (page == null) {
            render();
            Snackbar.make(binding.getRoot(), R.string.content_no_connection, Snackbar.LENGTH_SHORT).show();
            return;
        }
        HeroCache.replace(page, false);
        render();
    }

    private void loadMore() {
        List<Hero> page = repository.loadPage(HeroCache.size(), HeroRepository.PAGE_SIZE);
        if (page == null) {
            Snackbar.make(binding.getRoot(), R.string.content_no_connection, Snackbar.LENGTH_SHORT).show();
            return;
        }
        HeroCache.add(page);
        render();
    }

    private void restoreFromBackup() {
        List<Hero> heroes = backupManager.load();
        if (heroes == null || heroes.isEmpty()) {
            Toast.makeText(requireContext(), R.string.content_backup_broken, Toast.LENGTH_SHORT).show();
            render();
            return;
        }
        HeroCache.replace(heroes, true);
        render();
        Snackbar.make(binding.getRoot(), R.string.content_backup_restored, Snackbar.LENGTH_SHORT).show();
    }

    private void render() {
        boolean empty = HeroCache.isEmpty();

        binding.rvHeroes.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);

        if (empty) {
            binding.toolbar.setSubtitle(null);
            boolean hasBackup = backupManager.exists();
            binding.btnUseBackup.setVisibility(hasBackup ? View.VISIBLE : View.GONE);
            if (hasBackup) {
                String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(new Date(backupManager.getCreatedAt()));
                binding.tvEmptyMessage.setText(getString(R.string.content_empty_with_backup, date));
            } else {
                binding.tvEmptyMessage.setText(R.string.content_empty_no_backup);
            }
        } else {
            binding.toolbar.setSubtitle(getString(
                    HeroCache.isFromBackup()
                            ? R.string.content_subtitle_backup
                            : R.string.content_subtitle_count,
                    HeroCache.size()));
        }

        adapter.submit(HeroCache.get(), repository.hasMore(HeroCache.size()));
    }

    private void openSettings() {
        // Лаб. 1: startActivity(new Intent(this, SettingsActivity.class));
        host().navigateToSettings();
    }
}
