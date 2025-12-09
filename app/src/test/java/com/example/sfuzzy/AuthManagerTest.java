package com.example.sfuzzy;

import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AuthManagerTest {

    @Mock
    FirebaseAuth mockFirebaseAuth;

    @Mock
    Task<AuthResult> mockTask;

    @Mock
    AuthManager.OnRegistrationListener mockListener;

    private AuthManager authManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authManager = new AuthManager(mockFirebaseAuth);
    }

    @Test
    public void registerUser_emptyEmail_callsOnError() {
        authManager.registerUser("", "password", mockListener);
        verify(mockListener).onError("Enter email");
    }

    @Test
    public void registerUser_emptyPassword_callsOnError() {
        authManager.registerUser("test@mail.com", "", mockListener);
        verify(mockListener).onError("Enter password");
    }

    @Test
    public void registerUser_success_callsOnSuccess() {
        when(mockFirebaseAuth.createUserWithEmailAndPassword("test@mail.com", "pass"))
                .thenReturn(mockTask);
        
        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            when(mockTask.isSuccessful()).thenReturn(true);
            listener.onComplete(mockTask);
            return null;
        }).when(mockTask).addOnCompleteListener(any());

        authManager.registerUser("test@mail.com", "pass", mockListener);

        verify(mockListener).onSuccess();
    }

    @Test
    public void registerUser_failure_callsOnError() {
        when(mockFirebaseAuth.createUserWithEmailAndPassword("test@mail.com", "pass"))
                .thenReturn(mockTask);

        Exception fakeException = new Exception("Email already in use");

        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            when(mockTask.isSuccessful()).thenReturn(false);
            when(mockTask.getException()).thenReturn(fakeException);
            listener.onComplete(mockTask);
            return null;
        }).when(mockTask).addOnCompleteListener(any());

        authManager.registerUser("test@mail.com", "pass", mockListener);

        verify(mockListener).onError("Email already in use");
    }
}
