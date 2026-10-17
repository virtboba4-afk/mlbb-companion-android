package com.example.androidlab;

import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.androidlab.databinding.ActivityMainBinding;

/**
 * Единственная деятельность приложения: хостит экраны-фрагменты
 * и выполняет переходы между ними по графу навигации (res/navigation/nav_graph.xml).
 */
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

    /** Start → Registration; логин из поля стартового экрана передаётся через Safe Args. */
    public void navigateToRegistration(String login) {
        navController.navigate(StartFragmentDirections.actionStartToRegistration().setLogin(login));
    }

    /** Start → Content; стартовый экран закрывается (popUpTo в графе). */
    public void navigateToContent(String nickname) {
        navController.navigate(StartFragmentDirections.actionStartToContent(nickname));
    }

    /** Content → Settings. */
    public void navigateToSettings() {
        navController.navigate(ContentFragmentDirections.actionContentToSettings());
    }

    /** Возврат на предыдущий экран (Registration → Start, Settings → Content). */
    public void navigateBack() {
        navController.navigateUp();
    }

    /** Settings → Start с очисткой всего стека экранов. */
    public void navigateToStartAfterLogout() {
        navController.navigate(SettingsFragmentDirections.actionSettingsToStart());
    }
}
