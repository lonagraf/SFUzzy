package com.example.sfuzzy;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleAuthManager {

    public interface GoogleAuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String error);
    }

    private final FirebaseAuth mAuth;
    private final GoogleSignInClient googleSignInClient;
    private final Context context;

    public GoogleAuthManager(Context context, String clientId) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();

        this.googleSignInClient = GoogleSignIn.getClient(context, options);
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void handleSignInResult(Intent data, GoogleAuthCallback callback) {
        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(signInAccount, callback);
        } catch (ApiException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onError("Google sign in failed: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount signInAccount, GoogleAuthCallback callback) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (callback != null) {
                    callback.onSuccess(user);
                }
            } else {
                if (callback != null) {
                    callback.onError("Failed: " + task.getException().getMessage());
                }
            }
        });
    }

    public void signOut() {
        googleSignInClient.signOut();
    }

    }