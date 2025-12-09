// NavigationHelper.java
package com.example.sfuzzy;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

public class NavigationHelper {

    public static void navigateToFragment(FragmentActivity activity,
                                          int containerId,
                                          Fragment fragment,
                                          boolean addToBackStack) {
        FragmentTransaction transaction = activity
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }
}