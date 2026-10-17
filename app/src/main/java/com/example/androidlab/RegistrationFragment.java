package com.example.androidlab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.androidlab.data.AppPrefs;
import com.example.androidlab.databinding.FragmentRegistrationBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RegistrationFragment extends BaseFragment {

    /** Ключ результата регистрации для Fragment Result API. */
    public static final String REQUEST_KEY = "registration";

    public static final String RESULT_OK = "ok";
    public static final String RESULT_LOGIN = "login";
    public static final String RESULT_PASSWORD = "password";
    public static final String RESULT_NICKNAME = "nickname";

    private static final String LOGIN_PATTERN = "[A-Za-z0-9_]+";

    private FragmentRegistrationBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            // Логин, введённый на стартовом экране (передан через Safe Args)
            String login = RegistrationFragmentArgs.fromBundle(requireArguments()).getLogin();
            binding.etRegistrationLogin.setText(login);
        }

        binding.btnCompleteRegistration.setOnClickListener(v -> registerUser());

        binding.btnCancelRegistration.setOnClickListener(v -> cancel());

        // Системная кнопка "назад" тоже отменяет регистрацию (как RESULT_CANCELED у деятельности)
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        cancel();
                    }
                });
    }

    private void cancel() {
        // Лаб. 1:
        // setResult(RESULT_CANCELED);
        // finish();
        getParentFragmentManager().setFragmentResult(REQUEST_KEY, new Bundle());
        host().navigateBack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void registerUser() {

        String login = binding.etRegistrationLogin.getText().toString().trim();
        String password = binding.etRegistrationPassword.getText().toString().trim();
        String passwordConfirm = binding.etRegistrationPasswordConfirm.getText().toString().trim();
        String nickname = binding.etRegistrationNickname.getText().toString().trim();

        binding.registrationLoginLayout.setError(null);
        binding.registrationPasswordLayout.setError(null);
        binding.registrationPasswordConfirmLayout.setError(null);
        binding.registrationNicknameLayout.setError(null);

        if (login.isEmpty()) {
            binding.registrationLoginLayout.setError(getString(R.string.error_login_empty));
            return;
        }

        if (login.length() < 3) {
            binding.registrationLoginLayout.setError(getString(R.string.error_login_short));
            return;
        }

        if (!login.matches(LOGIN_PATTERN)) {
            binding.registrationLoginLayout.setError(getString(R.string.error_login_chars));
            return;
        }

        if (password.isEmpty()) {
            binding.registrationPasswordLayout.setError(getString(R.string.error_password_empty));
            return;
        }

        if (password.length() < 6) {
            binding.registrationPasswordLayout.setError(getString(R.string.error_password_short));
            return;
        }

        if (!password.equals(passwordConfirm)) {
            binding.registrationPasswordConfirmLayout.setError(getString(R.string.error_password_mismatch));
            return;
        }

        if (nickname.isEmpty()) {
            binding.registrationNicknameLayout.setError(getString(R.string.error_nickname_empty));
            return;
        }

        if (nickname.length() < 2 || nickname.length() > 20) {
            binding.registrationNicknameLayout.setError(getString(R.string.error_nickname_length));
            return;
        }

        AppPrefs prefs = new AppPrefs(requireContext());
        if (prefs.hasUser() && !prefs.getLogin().equals(login)) {
            // На устройстве хранится один аккаунт - предупреждаем о замене
            new MaterialAlertDialogBuilder(requireContext())
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
        // Лаб. 1:
        // Intent resultIntent = new Intent();
        // resultIntent.putExtra(EXTRA_LOGIN, login);
        // resultIntent.putExtra(EXTRA_PASSWORD, password);
        // resultIntent.putExtra(EXTRA_NICKNAME, nickname);
        // setResult(RESULT_OK, resultIntent);
        // finish();

        Bundle result = new Bundle();
        result.putBoolean(RESULT_OK, true);
        result.putString(RESULT_LOGIN, login);
        result.putString(RESULT_PASSWORD, password);
        result.putString(RESULT_NICKNAME, nickname);

        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
        host().navigateBack();
    }
}
