package com.example.sfuzzy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AuthNavigatorTest {

    @Mock FragmentActivity mockActivity;
    @Mock FragmentManager mockFragmentManager;
    @Mock FragmentTransaction mockTransaction;
    @Mock FirebaseUser mockUser;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Настраиваем моки FragmentManager/Transaction
        when(mockActivity.getSupportFragmentManager()).thenReturn(mockFragmentManager);
        when(mockFragmentManager.beginTransaction()).thenReturn(mockTransaction);

        when(mockTransaction.replace(anyInt(), any(Fragment.class))).thenReturn(mockTransaction);
        when(mockTransaction.addToBackStack(any())).thenReturn(mockTransaction);
    }


    @Test
    public void navigateToFragment_callsFragmentTransaction() {
        Fragment fragment = mock(Fragment.class);

        AuthNavigator.navigateToFragment(mockActivity, fragment, 456);

        verify(mockFragmentManager).beginTransaction();
        verify(mockTransaction).replace(456, fragment);
        verify(mockTransaction).addToBackStack(null);
        verify(mockTransaction).commit();
    }
}
