package com.example.edunet.di;
import android.app.Activity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.edunet.R;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.FragmentComponent;

@InstallIn(FragmentComponent.class)
@Module
public class NavigationModule {
    @Provides
    public NavController provideNavController(Activity activity){
        return Navigation.findNavController(activity, R.id.nav_host_fragment_activity_main);
    }
}
