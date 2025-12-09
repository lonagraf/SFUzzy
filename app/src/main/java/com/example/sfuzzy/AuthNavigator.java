package com.example.sfuzzy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseUser;

public class AuthNavigator {

    public static void navigateToTopics(FragmentActivity activity, FirebaseUser user, int containerId) {
        TopicsFragment topicsFragment = new TopicsFragment();
        Bundle args = new Bundle();
        args.putString("userName", user.getDisplayName());
        args.putString("userEmail", user.getEmail());
        args.putString("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
        topicsFragment.setArguments(args);

        FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerId, topicsFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public static void navigateToFragment(FragmentActivity activity, Fragment fragment, int containerId) {
        FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(containerId, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}