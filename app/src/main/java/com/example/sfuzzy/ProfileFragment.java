package com.example.sfuzzy;

import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvEmail, tvUserName, tvLessons;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration progressListener;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvEmail = view.findViewById(R.id.tvEmail);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvLessons = view.findViewById(R.id.tvStat1);
        btnLogout = view.findViewById(R.id.btnLogout);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            loadOrCreateUserDocument(user);
        } else {
            Toast.makeText(getContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
        }

        setupNameClick();
        setupLogout();

        Button btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private void loadOrCreateUserDocument(FirebaseUser user) {
        String uid = user.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Map<String, Object> initData = new HashMap<>();
                        initData.put("email", user.getEmail());
                        initData.put("name", "");
                        Map<String, Object> progress = new HashMap<>();
                        progress.put("lessonsCompleted", 0);
                        initData.put("progress", progress);
                        db.collection("users").document(uid).set(initData, SetOptions.merge());
                    }
                    // Подписка на прогресс пользователя
                    subscribeToProgressUpdates(uid);
                    // Отображаем имя сразу
                    String name = doc.getString("name");
                    if (name != null && !name.isEmpty()) tvUserName.setText(name);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show());
    }

    private void subscribeToProgressUpdates(String uid) {
        progressListener = db.collection("users").document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null && snapshot.exists()) {
                        DocumentSnapshot doc = snapshot;
                        Map<String, Object> progress = null;
                        Object obj = doc.get("progress");
                        if (obj instanceof Map) {
                            progress = (Map<String, Object>) obj;
                        } else {
                            progress = new HashMap<>();
                        }
                        long lessons = ((Number) progress.getOrDefault("lessonsCompleted", 0)).longValue();
                        tvLessons.setText("Lessons completed: " + lessons);
                    }
                });
    }

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

    private void saveUserName(String name) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !name.trim().isEmpty()) {
            String uid = user.getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);

            db.collection("users").document(uid)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> tvUserName.setText(name))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка при сохранении имени", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupLogout() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            if (progressListener != null) progressListener.remove();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_view_tag, new LoginFragment())
                    .commit();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressListener != null) progressListener.remove();
    }

}
