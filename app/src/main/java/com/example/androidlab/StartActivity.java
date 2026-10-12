package com.example.androidlab;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.androidlab.data.AppPrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class StartActivity extends BaseActivity {

    private AppPrefs prefs;

    private View root;
    private TextView tvWelcome;
    private TextView tvWelcomeHint;

    private TextInputLayout loginLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText etLogin;
    private TextInputEditText etPassword;

    /** Запуск регистрации с получением результата (логин, пароль, никнейм). */
    private final ActivityResultLauncher<Intent> registrationLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    this::onRegistrationResult
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        prefs = new AppPrefs(this);

        root = findViewById(R.id.startRoot);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcomeHint = findViewById(R.id.tvWelcomeHint);

        loginLayout = findViewById(R.id.loginLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);

        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> login());

        btnRegister.setOnClickListener(v -> openRegistration());

        // Поля ввода восстанавливаются системой сами, предзаполняем их только при первом запуске
        showUserState(savedInstanceState == null);
    }

    /**
     * Оформление экрана: первый вход (пустые поля) или вход известного пользователя
     * (приветствие по никнейму и предзаполненный логин).
     */
    private void showUserState(boolean fillLogin) {
        if (prefs.hasUser()) {
            tvWelcome.setText(getString(R.string.start_welcome_back, prefs.getNickname()));
            tvWelcomeHint.setText(R.string.start_hint_back);
            if (fillLogin) {
                etLogin.setText(prefs.getLogin());
                etPassword.requestFocus();
            }
        } else {
            tvWelcome.setText(R.string.start_welcome_new);
            tvWelcomeHint.setText(R.string.start_hint_new);
        }
    }

    private void login() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        loginLayout.setError(null);
        passwordLayout.setError(null);

        if (login.isEmpty()) {
            loginLayout.setError(getString(R.string.error_login_empty));
            return;
        }

        if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.error_password_empty));
            return;
        }

        if (!prefs.hasUser()) {
            Snackbar.make(root, R.string.start_no_user, Snackbar.LENGTH_LONG)
                    .setAction(R.string.start_register, v -> openRegistration())
                    .show();
            return;
        }

        if (!prefs.checkCredentials(login, password)) {
            passwordLayout.setError(getString(R.string.error_wrong_credentials));
            return;
        }

        startActivity(new Intent(this, ContentActivity.class));
        // Стартовый экран больше не нужен: по "назад" с экрана данных приложение закрывается
        finish();
    }

    private void openRegistration() {
        registrationLauncher.launch(new Intent(this, RegistrationActivity.class));
    }

    private void onRegistrationResult(ActivityResult result) {
        Intent data = result.getData();
        if (result.getResultCode() != RESULT_OK || data == null) {
            Snackbar.make(root, R.string.start_registration_cancelled, Snackbar.LENGTH_SHORT).show();
            return;
        }

        String login = data.getStringExtra(RegistrationActivity.EXTRA_LOGIN);
        String password = data.getStringExtra(RegistrationActivity.EXTRA_PASSWORD);
        String nickname = data.getStringExtra(RegistrationActivity.EXTRA_NICKNAME);

        prefs.saveUser(login, password, nickname);

        etPassword.setText("");
        showUserState(true);

        Snackbar.make(root, getString(R.string.start_registered, login), Snackbar.LENGTH_LONG).show();
    }
}
