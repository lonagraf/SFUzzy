package com.example.sfuzzy;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private TextView register;
    private EditText emailText, passwordText;
    private FirebaseAuth mAuth;
    private GoogleAuthManager googleAuthManager;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        if (googleAuthManager != null) {
                            googleAuthManager.handleSignInResult(
                                    result.getData(),
                                    new GoogleAuthManager.GoogleAuthCallback() {
                                        @Override
                                        public void onSuccess(FirebaseUser user) {
                                            if (user != null) {
                                                AuthNavigator.navigateToTopics(
                                                        requireActivity(),
                                                        user,
                                                        R.id.fragment_container_view_tag
                                                );
                                            }
                                            Toast.makeText(requireContext(), "Signed in successfully", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(String error) {
                                            Toast.makeText(requireContext(), "Failed: " + error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        }
                    }
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            AuthNavigator.navigateToTopics(
                    requireActivity(),
                    currentUser,
                    R.id.fragment_container_view_tag
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        register = view.findViewById(R.id.register);
        emailText = view.findViewById(R.id.loginEmail);
        passwordText = view.findViewById(R.id.loginPassword);
        Button loginBtn = view.findViewById(R.id.loginBtn);
        Button loginGoogleBtn = view.findViewById(R.id.loginGoogleBtn);

        // Инициализация GoogleAuthManager
        googleAuthManager = new GoogleAuthManager(
                requireContext(),
                getString(R.string.client_id)
        );

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthNavigator.navigateToFragment(
                        requireActivity(),
                        new RegisterFragment(),
                        R.id.fragment_container_view_tag
                );
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();

                if (email.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(requireContext(), "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                AuthNavigator.navigateToTopics(
                                        requireActivity(),
                                        user,
                                        R.id.fragment_container_view_tag
                                );
                            } else {
                                Toast.makeText(requireContext(), "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleAuthManager.getSignInIntent();
                activityResultLauncher.launch(intent);
            }
        });

        return view;
    }
}