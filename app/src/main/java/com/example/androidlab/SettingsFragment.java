package com.example.androidlab;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.androidlab.data.AppPrefs;
import com.example.androidlab.data.AvatarStorage;
import com.example.androidlab.data.BackupManager;
import com.example.androidlab.data.HeroCache;
import com.example.androidlab.databinding.DialogChangePasswordBinding;
import com.example.androidlab.databinding.FragmentSettingsBinding;
import com.example.androidlab.model.Hero;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class SettingsFragment extends BaseFragment {

    private static final String NAME_PATTERN = "[\\p{L} \\-]+";

    private AppPrefs prefs;
    private BackupManager backupManager;
    private AvatarStorage avatarStorage;

    private FragmentSettingsBinding binding;

    private final ActivityResultLauncher<String> pickAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onAvatarPicked);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = new AppPrefs(requireContext());
        backupManager = new BackupManager(requireContext());
        avatarStorage = new AvatarStorage(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Лаб. 1: возврат к экрану данных закрытием деятельности
        // toolbar.setNavigationOnClickListener(v -> finish());
        // findViewById(R.id.btnBackToContent).setOnClickListener(v -> finish());
        binding.toolbar.setNavigationOnClickListener(v -> host().navigateBack());
        binding.btnBackToContent.setOnClickListener(v -> host().navigateBack());
        binding.btnLogout.setOnClickListener(v -> logout());

        setupBackupSection();
        setupProfileSection(savedInstanceState == null);
        setupAppearanceSection();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ===================== Резервная копия =====================

    private void setupBackupSection() {
        binding.btnBackupCreate.setOnClickListener(v -> createBackup());
        binding.btnBackupRestore.setOnClickListener(v -> restoreBackup());
        binding.btnBackupDelete.setOnClickListener(v -> confirmDeleteBackup());

        binding.switchOffline.setChecked(prefs.isOfflineMode());
        binding.switchOffline.setOnCheckedChangeListener((button, checked) -> prefs.setOfflineMode(checked));

        renderBackupState();
    }

    /** Оформление кнопок зависит от наличия копии и загруженных данных. */
    private void renderBackupState() {
        boolean exists = backupManager.exists();
        boolean hasData = !HeroCache.isEmpty();

        if (exists) {
            String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(new Date(backupManager.getCreatedAt()));
            binding.tvBackupStatus.setText(getString(R.string.settings_backup_exists,
                    backupManager.getCount(), date));
        } else {
            binding.tvBackupStatus.setText(R.string.settings_backup_none);
        }
        if (!hasData) {
            binding.tvBackupStatus.append("\n\n" + getString(R.string.settings_backup_no_data));
        }

        binding.btnBackupCreate.setText(exists ? R.string.settings_backup_update : R.string.settings_backup_create);
        binding.btnBackupCreate.setEnabled(hasData);
        binding.btnBackupRestore.setVisibility(exists ? View.VISIBLE : View.GONE);
        binding.btnBackupDelete.setVisibility(exists ? View.VISIBLE : View.GONE);
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
        HeroCache.replaceFromBackup(heroes);
        Snackbar.make(binding.getRoot(), R.string.settings_backup_restored, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings_back, v -> host().navigateBack())
                .show();
        renderBackupState();
    }

    private void confirmDeleteBackup() {
        new MaterialAlertDialogBuilder(requireContext())
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
        binding.tvProfileLogin.setText(getString(R.string.settings_login, prefs.getLogin()));

        binding.btnPickAvatar.setOnClickListener(v -> pickAvatarLauncher.launch("image/*"));
        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        showAvatar();

        if (fillFields) {
            binding.etNickname.setText(prefs.getNickname());
            binding.etFullName.setText(prefs.getFullName());
            if (prefs.getAge() > 0) {
                binding.etAge.setText(String.valueOf(prefs.getAge()));
            }
            if (AppPrefs.GENDER_MALE.equals(prefs.getGender())) {
                binding.toggleGender.check(R.id.btnGenderMale);
            } else if (AppPrefs.GENDER_FEMALE.equals(prefs.getGender())) {
                binding.toggleGender.check(R.id.btnGenderFemale);
            }
        }
    }

    private void showAvatar() {
        Bitmap avatar = avatarStorage.load(256);
        if (avatar != null) {
            binding.ivAvatar.setImageBitmap(avatar);
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
        String nickname = binding.etNickname.getText().toString().trim();
        String fullName = binding.etFullName.getText().toString().trim();
        String ageText = binding.etAge.getText().toString().trim();

        binding.nicknameLayout.setError(null);
        binding.fullNameLayout.setError(null);
        binding.ageLayout.setError(null);

        if (nickname.isEmpty()) {
            binding.nicknameLayout.setError(getString(R.string.error_nickname_empty));
            return;
        }

        if (nickname.length() < 2 || nickname.length() > 20) {
            binding.nicknameLayout.setError(getString(R.string.error_nickname_length));
            return;
        }

        if (!fullName.isEmpty() && !fullName.matches(NAME_PATTERN)) {
            binding.fullNameLayout.setError(getString(R.string.error_full_name));
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
                binding.ageLayout.setError(getString(R.string.error_age));
                return;
            }
        }

        String gender = AppPrefs.GENDER_NONE;
        int checked = binding.toggleGender.getCheckedButtonId();
        if (checked == R.id.btnGenderMale) {
            gender = AppPrefs.GENDER_MALE;
        } else if (checked == R.id.btnGenderFemale) {
            gender = AppPrefs.GENDER_FEMALE;
        }

        prefs.saveProfile(nickname, fullName, gender, age);
        showMessage(R.string.settings_profile_saved);
    }

    private void showChangePasswordDialog() {
        DialogChangePasswordBinding dialogBinding = DialogChangePasswordBinding.inflate(getLayoutInflater());
        TextInputLayout oldLayout = dialogBinding.oldPasswordLayout;
        TextInputLayout newLayout = dialogBinding.newPasswordLayout;
        TextInputEditText etOld = dialogBinding.etOldPassword;
        TextInputEditText etNew = dialogBinding.etNewPassword;

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_change_password)
                .setView(dialogBinding.getRoot())
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
        int mode = prefs.getThemeMode();
        binding.toggleTheme.check(mode == AppCompatDelegate.MODE_NIGHT_NO ? R.id.btnThemeLight
                : mode == AppCompatDelegate.MODE_NIGHT_YES ? R.id.btnThemeDark
                : R.id.btnThemeSystem);
        binding.toggleTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            int newMode = checkedId == R.id.btnThemeLight ? AppCompatDelegate.MODE_NIGHT_NO
                    : checkedId == R.id.btnThemeDark ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            prefs.setThemeMode(newMode);
            // AppCompat сам пересоздаст открытые экраны
            AppCompatDelegate.setDefaultNightMode(newMode);
        });

        // Цветовая схема
        switch (prefs.getPalette()) {
            case AppPrefs.PALETTE_BLUE:
                binding.chipGroupPalette.check(R.id.chipPaletteBlue);
                break;
            case AppPrefs.PALETTE_GOLD:
                binding.chipGroupPalette.check(R.id.chipPaletteGold);
                break;
            case AppPrefs.PALETTE_GREEN:
                binding.chipGroupPalette.check(R.id.chipPaletteGreen);
                break;
            default:
                binding.chipGroupPalette.check(R.id.chipPalettePurple);
        }
        binding.chipGroupPalette.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            String palette = id == R.id.chipPaletteBlue ? AppPrefs.PALETTE_BLUE
                    : id == R.id.chipPaletteGold ? AppPrefs.PALETTE_GOLD
                    : id == R.id.chipPaletteGreen ? AppPrefs.PALETTE_GREEN
                    : AppPrefs.PALETTE_PURPLE;
            prefs.setPalette(palette);
            // Тема применяется при создании деятельности - пересоздаём её, стек экранов сохранится
            ((BaseActivity) requireActivity()).recreateIfAppearanceChanged();
        });

        // Размер шрифта
        binding.sliderFontSize.setValue(prefs.getFontScaleIndex());
        binding.sliderFontSize.setLabelFormatter(value -> fontPercent((int) value) + "%");
        binding.tvFontSize.setText(getString(R.string.settings_font_size, fontPercent(prefs.getFontScaleIndex())));
        binding.sliderFontSize.addOnChangeListener((slider, value, fromUser) ->
                binding.tvFontSize.setText(getString(R.string.settings_font_size, fontPercent((int) value))));
        binding.sliderFontSize.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                prefs.setFontScaleIndex((int) slider.getValue());
                ((BaseActivity) requireActivity()).recreateIfAppearanceChanged();
            }
        });

        // Язык
        String language = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        binding.toggleLanguage.check(language.startsWith("ru") ? R.id.btnLangRu
                : language.startsWith("en") ? R.id.btnLangEn
                : R.id.btnLangSystem);
        binding.toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
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
        // Лаб. 1:
        // Intent intent = new Intent(this, StartActivity.class);
        // // Закрываем все открытые экраны и начинаем со стартового
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // startActivity(intent);
        // finish();
        host().navigateToStartAfterLogout();
    }

    private void showMessage(int textRes) {
        Snackbar.make(binding.getRoot(), textRes, Snackbar.LENGTH_SHORT).show();
    }
}
