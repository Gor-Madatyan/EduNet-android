package com.example.edunet;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.databinding.ActivityMainBinding;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    @Inject
    AccountService accountService;
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.signInFragment, R.id.navigation_chats, R.id.navigation_search, R.id.navigation_profile)
                        .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavGraph graph = navController.getNavInflater().inflate(R.navigation.mobile_navigation);
        graph.setStartDestination(accountService.isUserAvailable() ? R.id.navigation_chats : R.id.signInFragment);
        navController.setGraph(graph);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavView, navController);

        navController.addOnDestinationChangedListener((controller, destination, bundle) -> {
            if (destination.getId() == R.id.chatFragment || destination.getId() == R.id.signInFragment || destination.getId() == R.id.signUpFragment)
                binding.bottomNavView.setVisibility(View.GONE);
            else binding.bottomNavView.setVisibility(View.VISIBLE);

        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


}