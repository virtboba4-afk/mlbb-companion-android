package com.example.androidlab;

import android.content.Intent;
import android.os.Bundle;

import com.example.androidlab.data.AppPrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegistrationActivity extends BaseActivity {

    public static final String EXTRA_LOGIN = "login";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_NICKNAME = "nickname";

    private static final String LOGIN_PATTERN = "[A-Za-z0-9_]+";

    private TextInputLayout loginLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout passwordConfirmLayout;
    private TextInputLayout nicknameLayout;

    private TextInputEditText etLogin;
    private TextInputEditText etPassword;
    private TextInputEditText etPasswordConfirm;
    private TextInputEditText etNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);

        loginLayout = findViewById(R.id.registrationLoginLayout);
        passwordLayout = findViewById(R.id.registrationPasswordLayout);
        passwordConfirmLayout = findViewById(R.id.registrationPasswordConfirmLayout);
        nicknameLayout = findViewById(R.id.registrationNicknameLayout);

        etLogin = findViewById(R.id.etRegistrationLogin);
        etPassword = findViewById(R.id.etRegistrationPassword);
        etPasswordConfirm = findViewById(R.id.etRegistrationPasswordConfirm);
        etNickname = findViewById(R.id.etRegistrationNickname);

        MaterialButton btnCompleteRegistration = findViewById(R.id.btnCompleteRegistration);
        MaterialButton btnCancelRegistration = findViewById(R.id.btnCancelRegistration);

        btnCompleteRegistration.setOnClickListener(v -> registerUser());

        btnCancelRegistration.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void registerUser() {

        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();

        loginLayout.setError(null);
        passwordLayout.setError(null);
        passwordConfirmLayout.setError(null);
        nicknameLayout.setError(null);

        if (login.isEmpty()) {
            loginLayout.setError(getString(R.string.error_login_empty));
            return;
        }

        if (login.length() < 3) {
            loginLayout.setError(getString(R.string.error_login_short));
            return;
        }

        if (!login.matches(LOGIN_PATTERN)) {
            loginLayout.setError(getString(R.string.error_login_chars));
            return;
        }

        if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.error_password_empty));
            return;
        }

        if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.error_password_short));
            return;
        }

        if (!password.equals(passwordConfirm)) {
            passwordConfirmLayout.setError(getString(R.string.error_password_mismatch));
            return;
        }

        if (nickname.isEmpty()) {
            nicknameLayout.setError(getString(R.string.error_nickname_empty));
            return;
        }

        if (nickname.length() < 2 || nickname.length() > 20) {
            nicknameLayout.setError(getString(R.string.error_nickname_length));
            return;
        }

        AppPrefs prefs = new AppPrefs(this);
        if (prefs.hasUser() && !prefs.getLogin().equals(login)) {
            // На устройстве хранится один аккаунт - предупреждаем о замене
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.registration_replace_title)
                    .setMessage(getString(R.string.registration_replace_message, prefs.getLogin()))
                    .setPositiveButton(R.string.registration_replace_confirm,
                            (dialog, which) -> finishWithResult(login, password, nickname))
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
            return;
        }

        finishWithResult(login, password, nickname);
    }

    private void finishWithResult(String login, String password, String nickname) {
        Intent resultIntent = new Intent();

        resultIntent.putExtra(EXTRA_LOGIN, login);
        resultIntent.putExtra(EXTRA_PASSWORD, password);
        resultIntent.putExtra(EXTRA_NICKNAME, nickname);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
