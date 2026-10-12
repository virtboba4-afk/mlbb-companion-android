package com.example.androidlab;

import android.content.Intent;
import android.os.Bundle;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegistrationActivity extends BaseActivity {

    private TextInputEditText etLogin;
    private TextInputEditText etPassword;
    private TextInputEditText etPasswordConfirm;
    private TextInputEditText etNickname;

    private MaterialButton btnCompleteRegistration;
    private MaterialButton btnCancelRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);

        etLogin = findViewById(R.id.etRegistrationLogin);
        etPassword = findViewById(R.id.etRegistrationPassword);
        etPasswordConfirm = findViewById(R.id.etRegistrationPasswordConfirm);
        etNickname = findViewById(R.id.etRegistrationNickname);

        btnCompleteRegistration = findViewById(R.id.btnCompleteRegistration);
        btnCancelRegistration = findViewById(R.id.btnCancelRegistration);

        btnCompleteRegistration.setOnClickListener(v -> registerUser());

        btnCancelRegistration.setOnClickListener(v -> finish());
    }

    private void registerUser() {

        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();

        if (login.isEmpty()) {
            etLogin.setError("Введите логин");
            return;
        }

        if (login.length() < 3) {
            etLogin.setError("Логин должен содержать минимум 3 символа");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Введите пароль");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Пароль должен содержать минимум 6 символов");
            return;
        }

        if (passwordConfirm.isEmpty()) {
            etPasswordConfirm.setError("Повторите пароль");
            return;
        }

        if (!password.equals(passwordConfirm)) {
            etPasswordConfirm.setError("Пароли не совпадают");
            return;
        }

        if (nickname.isEmpty()) {
            etNickname.setError("Введите никнейм");
            return;
        }

        Intent resultIntent = new Intent();

        resultIntent.putExtra("login", login);
        resultIntent.putExtra("password", password);
        resultIntent.putExtra("nickname", nickname);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}