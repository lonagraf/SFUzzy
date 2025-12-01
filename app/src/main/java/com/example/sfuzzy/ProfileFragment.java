package com.example.sfuzzy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private TextView tvEmail, tvUserName;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvEmail = view.findViewById(R.id.tvEmail);
        tvUserName = view.findViewById(R.id.tvUserName);
        btnLogout = view.findViewById(R.id.btnLogout);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            showEmail(user);
            loadOrCreateUserDocument(user);
        } else {
            Toast.makeText(getContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
        }

        setupNameClick();
        setupLogout();

        return view;
    }

    // Показываем email
    private void showEmail(FirebaseUser user) {
        tvEmail.setText(user.getEmail());
    }

    // Загружаем документ пользователя или создаём его
    private void loadOrCreateUserDocument(FirebaseUser user) {
        String uid = user.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            tvUserName.setText(name);
                        }
                    } else {
                        // Документа нет — создаём с базовыми полями
                        Map<String, Object> initData = new HashMap<>();
                        initData.put("email", user.getEmail());
                        initData.put("name", "");
                        initData.put("avatar", "");
                        initData.put("progress", new HashMap<>());

                        db.collection("users").document(uid)
                                .set(initData)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Документ пользователя создан"))
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Ошибка создания документа: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "Ошибка создания документа", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Ошибка загрузки документа", e);
                });
    }

    // Настройка редактирования имени
    private void setupNameClick() {
        tvUserName.setOnClickListener(v -> {
            EditText editText = new EditText(getContext());
            editText.setText(tvUserName.getText().toString());

            new AlertDialog.Builder(requireContext())
                    .setTitle("Изменить имя")
                    .setView(editText)
                    .setPositiveButton("Сохранить", (dialog, which) -> saveUserName(editText.getText().toString()))
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    // Сохраняем имя в Firestore без перезаписи других полей
    private void saveUserName(String name) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !name.trim().isEmpty()) {
            String uid = user.getUid();

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);

            db.collection("users").document(uid)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> tvUserName.setText(name))
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Ошибка при сохранении имени: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Ошибка обновления имени", e);
                    });
        } else {
            Toast.makeText(getContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show();
        }
    }

    // Кнопка выхода
    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_view_tag, new LoginFragment())
                    .commit();
        });
    }
}
