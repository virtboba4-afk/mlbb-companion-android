package com.example.androidlab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.androidlab.data.AppPrefs;
import com.example.androidlab.databinding.FragmentStartBinding;
import com.google.android.material.snackbar.Snackbar;

public class StartFragment extends BaseFragment {

    private static final String STATE_LOGIN_FILLED = "login_filled";

    private AppPrefs prefs;
    private FragmentStartBinding binding;

    private boolean loginFilled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = new AppPrefs(requireContext());
        if (savedInstanceState != null) {
            loginFilled = savedInstanceState.getBoolean(STATE_LOGIN_FILLED);
        }

        getParentFragmentManager().setFragmentResultListener(
                RegistrationFragment.REQUEST_KEY, this, (key, result) -> onRegistrationResult(result));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentStartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnLogin.setOnClickListener(v -> login());
        binding.btnRegister.setOnClickListener(v -> openRegistration());

        showUserState();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_LOGIN_FILLED, loginFilled);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showUserState() {
        if (prefs.hasUser()) {
            binding.tvWelcome.setText(getString(R.string.start_welcome_back, prefs.getNickname()));
            binding.tvWelcomeHint.setText(R.string.start_hint_back);
            if (!loginFilled) {
                binding.etLogin.setText(prefs.getLogin());
                binding.etPassword.requestFocus();
                loginFilled = true;
            }
        } else {
            binding.tvWelcome.setText(R.string.start_welcome_new);
            binding.tvWelcomeHint.setText(R.string.start_hint_new);
        }
    }

    private void login() {
        String login = binding.etLogin.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        binding.loginLayout.setError(null);
        binding.passwordLayout.setError(null);

        if (login.isEmpty()) {
            binding.loginLayout.setError(getString(R.string.error_login_empty));
            return;
        }

        if (password.isEmpty()) {
            binding.passwordLayout.setError(getString(R.string.error_password_empty));
            return;
        }

        if (!prefs.hasUser()) {
            Snackbar.make(binding.getRoot(), R.string.start_no_user, Snackbar.LENGTH_LONG)
                    .setAction(R.string.start_register, v -> openRegistration())
                    .show();
            return;
        }

        if (!prefs.checkCredentials(login, password)) {
            binding.passwordLayout.setError(getString(R.string.error_wrong_credentials));
            return;
        }

        host().navigateToContent(prefs.getNickname());
    }

    private void openRegistration() {
        host().navigateToRegistration(binding.etLogin.getText().toString().trim());
    }

    private void onRegistrationResult(Bundle result) {
        if (!result.getBoolean(RegistrationFragment.RESULT_OK)) {
            Snackbar.make(binding.getRoot(), R.string.start_registration_cancelled, Snackbar.LENGTH_SHORT).show();
            return;
        }

        String login = result.getString(RegistrationFragment.RESULT_LOGIN);
        String password = result.getString(RegistrationFragment.RESULT_PASSWORD);
        String nickname = result.getString(RegistrationFragment.RESULT_NICKNAME);

        prefs.saveUser(login, password, nickname);

        binding.loginLayout.setError(null);
        binding.passwordLayout.setError(null);
        binding.etPassword.setText("");
        loginFilled = false;
        showUserState();

        Snackbar.make(binding.getRoot(), getString(R.string.start_registered, login), Snackbar.LENGTH_LONG).show();
    }
}
