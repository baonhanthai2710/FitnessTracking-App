package com.example.fitnesstrackingapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabPagerAdapter extends FragmentStateAdapter {
    private static final int TAB_COUNT = 5; 
    
    public TabPagerAdapter(FragmentActivity activity) {
        super(activity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DashboardFragment();
            case 1:
                return new QuickAccessFragment();
            case 2:
                return new EventManagementFragment();
            case 3:
                return new WorklogFragment(); 
            case 4:
                return new ChatbotFragment();
            default:
                return new DashboardFragment();
        }
    }
    
    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}