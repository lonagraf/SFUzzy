package com.example.sfuzzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class RegisterFragment extends Fragment {

    private EditText emailText, passwordText;
    private Button btnReg;
    private AuthManager authManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Инициализация
        emailText = view.findViewById(R.id.emailText);
        passwordText = view.findViewById(R.id.passwordText);
        btnReg = view.findViewById(R.id.registrationBtn);
        authManager = new AuthManager();

        // Обработчик нажатия
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        return view;
    }

    private void registerUser() {
        // Получаем данные
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        // Валидация
        InputValidator.ValidationResult validation =
                InputValidator.validateRegistrationInput(email, password);

        if (!validation.isValid()) {
            Toast.makeText(getContext(), validation.getErrorMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Регистрация
        authManager.registerUser(email, password, new AuthManager.OnRegistrationListener() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Registration successful!",
                                    Toast.LENGTH_SHORT).show();
                            goToLoginFragment();
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), errorMessage,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void goToLoginFragment() {
        if (getActivity() == null) return;

        FragmentTransaction transaction = getActivity()
                .getSupportFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.fragment_container_view_tag, new LoginFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}