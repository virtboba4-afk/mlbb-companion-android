package com.example.androidlab;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.androidlab.data.AppPrefs;
import com.example.androidlab.data.AvatarStorage;
import com.example.androidlab.data.BackupManager;
import com.example.androidlab.data.HeroCache;
import com.example.androidlab.model.Hero;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class SettingsActivity extends BaseActivity {

    private static final String NAME_PATTERN = "[\\p{L} \\-]+";

    private AppPrefs prefs;
    private BackupManager backupManager;
    private AvatarStorage avatarStorage;

    private View root;

    private TextView tvBackupStatus;
    private MaterialButton btnBackupCreate;
    private MaterialButton btnBackupRestore;
    private MaterialButton btnBackupDelete;

    private ShapeableImageView ivAvatar;
    private TextInputLayout nicknameLayout;
    private TextInputLayout fullNameLayout;
    private TextInputLayout ageLayout;
    private TextInputEditText etNickname;
    private TextInputEditText etFullName;
    private TextInputEditText etAge;
    private MaterialButtonToggleGroup toggleGender;

    private final ActivityResultLauncher<String> pickAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onAvatarPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        prefs = new AppPrefs(this);
        backupManager = new BackupManager(this);
        avatarStorage = new AvatarStorage(this);

        root = findViewById(R.id.settingsRoot);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        findViewById(R.id.btnBackToContent).setOnClickListener(v -> finish());
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        setupBackupSection();
        setupProfileSection(savedInstanceState == null);
        setupAppearanceSection();
    }

    // ===================== Резервная копия =====================

    private void setupBackupSection() {
        tvBackupStatus = findViewById(R.id.tvBackupStatus);
        btnBackupCreate = findViewById(R.id.btnBackupCreate);
        btnBackupRestore = findViewById(R.id.btnBackupRestore);
        btnBackupDelete = findViewById(R.id.btnBackupDelete);
        MaterialSwitch switchOffline = findViewById(R.id.switchOffline);

        btnBackupCreate.setOnClickListener(v -> createBackup());
        btnBackupRestore.setOnClickListener(v -> restoreBackup());
        btnBackupDelete.setOnClickListener(v -> confirmDeleteBackup());

        switchOffline.setChecked(prefs.isOfflineMode());
        switchOffline.setOnCheckedChangeListener((button, checked) -> prefs.setOfflineMode(checked));

        renderBackupState();
    }

    /** Оформление кнопок зависит от наличия копии и загруженных данных. */
    private void renderBackupState() {
        boolean exists = backupManager.exists();
        boolean hasData = !HeroCache.isEmpty();

        if (exists) {
            String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(new Date(backupManager.getCreatedAt()));
            tvBackupStatus.setText(getString(R.string.settings_backup_exists,
                    backupManager.getCount(), date));
        } else {
            tvBackupStatus.setText(R.string.settings_backup_none);
        }
        if (!hasData) {
            tvBackupStatus.append("\n\n" + getString(R.string.settings_backup_no_data));
        }

        btnBackupCreate.setText(exists ? R.string.settings_backup_update : R.string.settings_backup_create);
        btnBackupCreate.setEnabled(hasData);
        btnBackupRestore.setVisibility(exists ? View.VISIBLE : View.GONE);
        btnBackupDelete.setVisibility(exists ? View.VISIBLE : View.GONE);
    }

    private void createBackup() {
        boolean ok = backupManager.save(HeroCache.get());
        showMessage(ok ? R.string.settings_backup_created : R.string.settings_backup_error);
        renderBackupState();
    }

    private void restoreBackup() {
        List<Hero> heroes = backupManager.load();
        if (heroes == null || heroes.isEmpty()) {
            showMessage(R.string.content_backup_broken);
            return;
        }
        HeroCache.replace(heroes, true);
        Snackbar.make(root, R.string.settings_backup_restored, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings_back, v -> finish())
                .show();
        renderBackupState();
    }

    private void confirmDeleteBackup() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_backup_delete_title)
                .setMessage(R.string.settings_backup_delete_message)
                .setPositiveButton(R.string.settings_delete, (dialog, which) -> {
                    boolean ok = backupManager.delete();
                    showMessage(ok ? R.string.settings_backup_deleted : R.string.settings_backup_error);
                    renderBackupState();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    // ===================== Профиль =====================

    private void setupProfileSection(boolean fillFields) {
        ivAvatar = findViewById(R.id.ivAvatar);
        nicknameLayout = findViewById(R.id.nicknameLayout);
        fullNameLayout = findViewById(R.id.fullNameLayout);
        ageLayout = findViewById(R.id.ageLayout);
        etNickname = findViewById(R.id.etNickname);
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        toggleGender = findViewById(R.id.toggleGender);
        TextView tvProfileLogin = findViewById(R.id.tvProfileLogin);

        tvProfileLogin.setText(getString(R.string.settings_login, prefs.getLogin()));

        findViewById(R.id.btnPickAvatar).setOnClickListener(v -> pickAvatarLauncher.launch("image/*"));
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile());
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());

        showAvatar();

        if (fillFields) {
            etNickname.setText(prefs.getNickname());
            etFullName.setText(prefs.getFullName());
            if (prefs.getAge() > 0) {
                etAge.setText(String.valueOf(prefs.getAge()));
            }
            if (AppPrefs.GENDER_MALE.equals(prefs.getGender())) {
                toggleGender.check(R.id.btnGenderMale);
            } else if (AppPrefs.GENDER_FEMALE.equals(prefs.getGender())) {
                toggleGender.check(R.id.btnGenderFemale);
            }
        }
    }

    private void showAvatar() {
        Bitmap avatar = avatarStorage.load(256);
        if (avatar != null) {
            ivAvatar.setImageBitmap(avatar);
        }
    }

    private void onAvatarPicked(Uri uri) {
        if (uri == null) {
            return;
        }
        if (avatarStorage.save(uri)) {
            showAvatar();
            showMessage(R.string.settings_avatar_saved);
        } else {
            showMessage(R.string.settings_backup_error);
        }
    }

    private void saveProfile() {
        String nickname = etNickname.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String ageText = etAge.getText().toString().trim();

        nicknameLayout.setError(null);
        fullNameLayout.setError(null);
        ageLayout.setError(null);

        if (nickname.isEmpty()) {
            nicknameLayout.setError(getString(R.string.error_nickname_empty));
            return;
        }

        if (nickname.length() < 2 || nickname.length() > 20) {
            nicknameLayout.setError(getString(R.string.error_nickname_length));
            return;
        }

        if (!fullName.isEmpty() && !fullName.matches(NAME_PATTERN)) {
            fullNameLayout.setError(getString(R.string.error_full_name));
            return;
        }

        int age = 0;
        if (!ageText.isEmpty()) {
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException e) {
                age = -1;
            }
            if (age < 1 || age > 120) {
                ageLayout.setError(getString(R.string.error_age));
                return;
            }
        }

        String gender = AppPrefs.GENDER_NONE;
        int checked = toggleGender.getCheckedButtonId();
        if (checked == R.id.btnGenderMale) {
            gender = AppPrefs.GENDER_MALE;
        } else if (checked == R.id.btnGenderFemale) {
            gender = AppPrefs.GENDER_FEMALE;
        }

        prefs.saveProfile(nickname, fullName, gender, age);
        showMessage(R.string.settings_profile_saved);
    }

    private void showChangePasswordDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        TextInputLayout oldLayout = view.findViewById(R.id.oldPasswordLayout);
        TextInputLayout newLayout = view.findViewById(R.id.newPasswordLayout);
        TextInputEditText etOld = view.findViewById(R.id.etOldPassword);
        TextInputEditText etNew = view.findViewById(R.id.etNewPassword);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_change_password)
                .setView(view)
                .setPositiveButton(R.string.settings_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(d ->
                // Своя обработка, чтобы диалог не закрывался при ошибке ввода
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String oldPassword = etOld.getText().toString().trim();
                    String newPassword = etNew.getText().toString().trim();
                    oldLayout.setError(null);
                    newLayout.setError(null);

                    if (!prefs.checkCredentials(prefs.getLogin(), oldPassword)) {
                        oldLayout.setError(getString(R.string.error_wrong_password));
                        return;
                    }
                    if (newPassword.length() < 6) {
                        newLayout.setError(getString(R.string.error_password_short));
                        return;
                    }
                    prefs.setPassword(newPassword);
                    dialog.dismiss();
                    showMessage(R.string.settings_password_changed);
                }));

        dialog.show();
    }

    // ===================== Оформление =====================

    private void setupAppearanceSection() {
        // Сначала выставляем текущие значения, потом вешаем слушатели,
        // чтобы программная установка не считалась изменением

        // Тема
        MaterialButtonToggleGroup toggleTheme = findViewById(R.id.toggleTheme);
        int mode = prefs.getThemeMode();
        toggleTheme.check(mode == AppCompatDelegate.MODE_NIGHT_NO ? R.id.btnThemeLight
                : mode == AppCompatDelegate.MODE_NIGHT_YES ? R.id.btnThemeDark
                : R.id.btnThemeSystem);
        toggleTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            int newMode = checkedId == R.id.btnThemeLight ? AppCompatDelegate.MODE_NIGHT_NO
                    : checkedId == R.id.btnThemeDark ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            prefs.setThemeMode(newMode);
            // AppCompat сам пересоздаст открытые экраны
            AppCompatDelegate.setDefaultNightMode(newMode);
        });

        // Цветовая схема
        ChipGroup chipGroupPalette = findViewById(R.id.chipGroupPalette);
        switch (prefs.getPalette()) {
            case AppPrefs.PALETTE_BLUE:
                chipGroupPalette.check(R.id.chipPaletteBlue);
                break;
            case AppPrefs.PALETTE_GOLD:
                chipGroupPalette.check(R.id.chipPaletteGold);
                break;
            case AppPrefs.PALETTE_GREEN:
                chipGroupPalette.check(R.id.chipPaletteGreen);
                break;
            default:
                chipGroupPalette.check(R.id.chipPalettePurple);
        }
        chipGroupPalette.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            String palette = id == R.id.chipPaletteBlue ? AppPrefs.PALETTE_BLUE
                    : id == R.id.chipPaletteGold ? AppPrefs.PALETTE_GOLD
                    : id == R.id.chipPaletteGreen ? AppPrefs.PALETTE_GREEN
                    : AppPrefs.PALETTE_PURPLE;
            prefs.setPalette(palette);
            recreateIfAppearanceChanged();
        });

        // Размер шрифта
        TextView tvFontSize = findViewById(R.id.tvFontSize);
        Slider sliderFontSize = findViewById(R.id.sliderFontSize);
        sliderFontSize.setValue(prefs.getFontScaleIndex());
        sliderFontSize.setLabelFormatter(value -> fontPercent((int) value) + "%");
        tvFontSize.setText(getString(R.string.settings_font_size, fontPercent(prefs.getFontScaleIndex())));
        sliderFontSize.addOnChangeListener((slider, value, fromUser) ->
                tvFontSize.setText(getString(R.string.settings_font_size, fontPercent((int) value))));
        sliderFontSize.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                prefs.setFontScaleIndex((int) slider.getValue());
                recreateIfAppearanceChanged();
            }
        });

        // Язык
        MaterialButtonToggleGroup toggleLanguage = findViewById(R.id.toggleLanguage);
        String language = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        toggleLanguage.check(language.startsWith("ru") ? R.id.btnLangRu
                : language.startsWith("en") ? R.id.btnLangEn
                : R.id.btnLangSystem);
        toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String tags = checkedId == R.id.btnLangRu ? "ru"
                    : checkedId == R.id.btnLangEn ? "en"
                    : "";
            // AppCompat сохранит выбор и пересоздаст открытые экраны
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags));
        });
    }

    private static int fontPercent(int index) {
        return Math.round(AppPrefs.FONT_SCALES[index] * 100);
    }

    // ===================== Прочее =====================

    private void logout() {
        HeroCache.clear();
        Intent intent = new Intent(this, StartActivity.class);
        // Закрываем все открытые экраны и начинаем со стартового
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showMessage(int textRes) {
        Snackbar.make(root, textRes, Snackbar.LENGTH_SHORT).show();
    }
}
