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
import com.example.androidlab.network.HeroStatsResponse;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;

public class ContentFragment extends BaseFragment {

    private static final String STATE_GREETED = "greeted";

    private HeroRepository repository;
    private BackupManager backupManager;
    private HeroAdapter adapter;

    private FragmentContentBinding binding;

    /** Приветствие показывается один раз после входа, а не при каждом возврате с настроек. */
    private boolean greeted;

    /** Текущий сетевой запрос (отменяется, когда экран уничтожает разметку). */
    @Nullable
    private Call<HeroStatsResponse> currentCall;
    private boolean loadingFirstPage;
    private boolean loadingMore;

    /** Текст последней ошибки загрузки первой порции - показывается в состоянии "нет данных". */
    @Nullable
    private String lastError;

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

        // Запрос на сервер при открытии экрана, если данных ещё нет
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
        // Ответ на незавершённый запрос показывать уже негде
        if (currentCall != null) {
            currentCall.cancel();
            currentCall = null;
        }
        loadingFirstPage = false;
        loadingMore = false;
        binding = null;
    }

    private void loadFirstPage() {
        if (loadingFirstPage) {
            return;
        }
        loadingFirstPage = true;
        render();

        currentCall = repository.loadPage(0, HeroRepository.PAGE_SIZE, new HeroRepository.PageCallback() {
            @Override
            public void onPage(@NonNull List<Hero> page, int total) {
                currentCall = null;
                loadingFirstPage = false;
                lastError = null;
                HeroCache.replace(page, total);
                render();
            }

            @Override
            public void onError(@NonNull HeroRepository.ErrorType type, int httpCode) {
                currentCall = null;
                loadingFirstPage = false;
                lastError = errorText(type, httpCode);
                render();
            }
        });
    }

    private void loadMore() {
        if (loadingMore || loadingFirstPage) {
            return;
        }
        loadingMore = true;
        render();

        currentCall = repository.loadPage(HeroCache.size(), HeroRepository.PAGE_SIZE,
                new HeroRepository.PageCallback() {
                    @Override
                    public void onPage(@NonNull List<Hero> page, int total) {
                        currentCall = null;
                        loadingMore = false;
                        HeroCache.add(page, total);
                        render();
                    }

                    @Override
                    public void onError(@NonNull HeroRepository.ErrorType type, int httpCode) {
                        currentCall = null;
                        loadingMore = false;
                        render();
                        Snackbar.make(binding.getRoot(), errorText(type, httpCode), Snackbar.LENGTH_LONG)
                                .setAction(R.string.content_retry, v -> loadMore())
                                .show();
                    }
                });
    }

    private String errorText(HeroRepository.ErrorType type, int httpCode) {
        switch (type) {
            case NO_CONNECTION:
                return getString(R.string.content_no_connection);
            case HTTP:
                return getString(R.string.content_error_http, httpCode);
            case BAD_RESPONSE:
                return getString(R.string.content_error_bad_response);
            case EMPTY:
                return getString(R.string.content_error_empty);
            case NETWORK:
            default:
                return getString(R.string.content_error_network);
        }
    }

    private void restoreFromBackup() {
        List<Hero> heroes = backupManager.load();
        if (heroes == null || heroes.isEmpty()) {
            Toast.makeText(requireContext(), R.string.content_backup_broken, Toast.LENGTH_SHORT).show();
            render();
            return;
        }
        HeroCache.replaceFromBackup(heroes);
        render();
        Snackbar.make(binding.getRoot(), R.string.content_backup_restored, Snackbar.LENGTH_SHORT).show();
    }

    private void render() {
        if (binding == null) {
            return;
        }
        boolean empty = HeroCache.isEmpty();
        boolean showLoading = empty && loadingFirstPage;

        binding.loadingState.setVisibility(showLoading ? View.VISIBLE : View.GONE);
        binding.rvHeroes.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.emptyState.setVisibility(empty && !showLoading ? View.VISIBLE : View.GONE);

        if (empty) {
            binding.toolbar.setSubtitle(null);
            String error = lastError != null ? lastError : getString(R.string.content_no_connection);
            boolean hasBackup = backupManager.exists();
            binding.btnUseBackup.setVisibility(hasBackup ? View.VISIBLE : View.GONE);
            if (hasBackup) {
                String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(new Date(backupManager.getCreatedAt()));
                binding.tvEmptyMessage.setText(getString(R.string.content_empty_with_backup, error, date));
            } else {
                binding.tvEmptyMessage.setText(getString(R.string.content_empty_no_backup, error));
            }
        } else if (HeroCache.isFromBackup()) {
            binding.toolbar.setSubtitle(getString(R.string.content_subtitle_backup, HeroCache.size()));
        } else {
            binding.toolbar.setSubtitle(getString(R.string.content_subtitle_count,
                    HeroCache.size(), HeroCache.getTotal()));
        }

        adapter.submit(HeroCache.get(), HeroCache.hasMore(), loadingMore);
    }

    private void openSettings() {
        // Лаб. 1: startActivity(new Intent(this, SettingsActivity.class));
        host().navigateToSettings();
    }
}
