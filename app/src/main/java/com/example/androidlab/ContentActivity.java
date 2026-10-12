package com.example.androidlab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidlab.data.BackupManager;
import com.example.androidlab.data.HeroCache;
import com.example.androidlab.data.HeroRepository;
import com.example.androidlab.model.Hero;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ContentActivity extends BaseActivity {

    private HeroRepository repository;
    private BackupManager backupManager;
    private HeroAdapter adapter;

    private View root;
    private MaterialToolbar toolbar;
    private RecyclerView rvHeroes;
    private View emptyState;
    private TextView tvEmptyMessage;
    private MaterialButton btnUseBackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_content);

        repository = new HeroRepository(this);
        backupManager = new BackupManager(this);

        root = findViewById(R.id.contentRoot);
        toolbar = findViewById(R.id.toolbar);
        rvHeroes = findViewById(R.id.rvHeroes);
        emptyState = findViewById(R.id.emptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnUseBackup = findViewById(R.id.btnUseBackup);
        MaterialButton btnRetry = findViewById(R.id.btnRetry);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                openSettings();
                return true;
            }
            return false;
        });

        adapter = new HeroAdapter(this::loadMore);
        rvHeroes.setLayoutManager(new LinearLayoutManager(this));
        rvHeroes.setAdapter(adapter);

        btnRetry.setOnClickListener(v -> loadFirstPage());
        btnUseBackup.setOnClickListener(v -> restoreFromBackup());

        if (HeroCache.isEmpty()) {
            loadFirstPage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Данные могли измениться на экране настроек (восстановление из копии)
        render();
    }

    private void loadFirstPage() {
        List<Hero> page = repository.loadPage(0, HeroRepository.PAGE_SIZE);
        if (page == null) {
            render();
            Snackbar.make(root, R.string.content_no_connection, Snackbar.LENGTH_SHORT).show();
            return;
        }
        HeroCache.replace(page, false);
        render();
    }

    private void loadMore() {
        List<Hero> page = repository.loadPage(HeroCache.size(), HeroRepository.PAGE_SIZE);
        if (page == null) {
            Snackbar.make(root, R.string.content_no_connection, Snackbar.LENGTH_SHORT).show();
            return;
        }
        HeroCache.add(page);
        render();
    }

    private void restoreFromBackup() {
        List<Hero> heroes = backupManager.load();
        if (heroes == null || heroes.isEmpty()) {
            Toast.makeText(this, R.string.content_backup_broken, Toast.LENGTH_SHORT).show();
            render();
            return;
        }
        HeroCache.replace(heroes, true);
        render();
        Snackbar.make(root, R.string.content_backup_restored, Snackbar.LENGTH_SHORT).show();
    }

    private void render() {
        boolean empty = HeroCache.isEmpty();

        rvHeroes.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);

        if (empty) {
            toolbar.setSubtitle(null);
            boolean hasBackup = backupManager.exists();
            btnUseBackup.setVisibility(hasBackup ? View.VISIBLE : View.GONE);
            if (hasBackup) {
                String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(new Date(backupManager.getCreatedAt()));
                tvEmptyMessage.setText(getString(R.string.content_empty_with_backup, date));
            } else {
                tvEmptyMessage.setText(R.string.content_empty_no_backup);
            }
        } else {
            toolbar.setSubtitle(getString(
                    HeroCache.isFromBackup()
                            ? R.string.content_subtitle_backup
                            : R.string.content_subtitle_count,
                    HeroCache.size()));
        }

        adapter.submit(HeroCache.get(), repository.hasMore(HeroCache.size()));
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}
