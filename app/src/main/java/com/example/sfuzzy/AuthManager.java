package com.example.sfuzzy;

import com.google.firebase.auth.FirebaseAuth;

public class AuthManager {
    private final FirebaseAuth mAuth;

    public AuthManager() {
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void registerUser(String email, String password, OnRegistrationListener listener) {
        if (email == null || email.isEmpty()) {
            listener.onError("Enter email");
            return;
        }

        if (password == null || password.isEmpty()) {
            listener.onError("Enter password");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        String errorMessage = "Registration failed";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        listener.onError(errorMessage);
                    }
                });
    }

    // Интерфейс для обратного вызова
    public interface OnRegistrationListener {
        void onSuccess();
        void onError(String errorMessage);
    }
    public AuthManager(FirebaseAuth auth) {
        this.mAuth = auth;
    }
}