package com.example.androidlab;

import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.androidlab.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(binding.navHostFragment.getId());
        navController = navHostFragment.getNavController();
    }

    public void navigateToRegistration(String login) {
        navController.navigate(StartFragmentDirections.actionStartToRegistration().setLogin(login));
    }

    public void navigateToContent(String nickname) {
        navController.navigate(StartFragmentDirections.actionStartToContent(nickname));
    }

    public void navigateToSettings() {
        navController.navigate(ContentFragmentDirections.actionContentToSettings());
    }

    public void navigateBack() {
        navController.navigateUp();
    }

    public void navigateToStartAfterLogout() {
        navController.navigate(SettingsFragmentDirections.actionSettingsToStart());
    }
}
