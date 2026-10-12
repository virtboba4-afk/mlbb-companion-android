package com.example.androidlab;

import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class StartActivity extends AppCompatActivity {

    private TextInputEditText etLogin;
    private TextInputEditText etPassword;

    private MaterialButton btnLogin;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> login());

        btnRegister.setOnClickListener(v -> openRegistration());
    }

    private void login() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (login.isEmpty()) {
            etLogin.setError("Введите логин");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Введите пароль");
            return;
        }

        Toast.makeText(
                this,
                "Авторизация выполнена",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void openRegistration() {
        Toast.makeText(
                this,
                "Переход к регистрации",
                Toast.LENGTH_SHORT
        ).show();
    }

    
}