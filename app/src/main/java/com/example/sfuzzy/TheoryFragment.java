package com.example.sfuzzy;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TheoryFragment extends Fragment {

    private static final String ARG_TOPIC_NAME = "topic_name";
    private String topicName;
    private TextView tvTheoryContent;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    public static TheoryFragment newInstance(String topicName) {
        TheoryFragment fragment = new TheoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TOPIC_NAME, topicName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            topicName = getArguments().getString(ARG_TOPIC_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_theory, container, false);

        tvTheoryContent = view.findViewById(R.id.tvTheoryContent);
        progressBar = view.findViewById(R.id.progressBar);

        // Находим ScrollView (родительский элемент для TextView)
        scrollView = (ScrollView) tvTheoryContent.getParent();

        // Показываем ProgressBar, скрываем ScrollView
        progressBar.setVisibility(View.VISIBLE);
        if (scrollView != null) {
            scrollView.setVisibility(View.GONE);
        }

        // Загружаем теорию из Realtime Database
        loadTheoryFromRealtimeDatabase();

        Button btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private void loadTheoryFromRealtimeDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("topics")
                .child(topicName)
                .child("theory");

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Скрываем ProgressBar, показываем контент
                progressBar.setVisibility(View.GONE);
                if (scrollView != null) {
                    scrollView.setVisibility(View.VISIBLE);
                }

                String theoryText = snapshot.getValue(String.class);
                if (theoryText != null && !theoryText.isEmpty()) {
                    tvTheoryContent.setText(Html.fromHtml(theoryText, Html.FROM_HTML_MODE_LEGACY));
                    updateProgress(); // Обновляем прогресс после прочтения теории
                } else {
                    tvTheoryContent.setText("Теория пока не добавлена");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                if (scrollView != null) {
                    scrollView.setVisibility(View.VISIBLE);
                }
                tvTheoryContent.setText("Ошибка загрузки теории: " + error.getMessage());
            }
        });
    }

    private void updateProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> progress = (Map<String, Object>) documentSnapshot.get("progress");
                        if (progress == null) progress = new HashMap<>();

                        long lessonsCompleted = ((Number) progress.getOrDefault("lessonsCompleted", 0)).longValue();
                        lessonsCompleted++; // прибавляем один пройденный урок

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("progress.lessonsCompleted", lessonsCompleted);

                        db.collection("users").document(uid)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    // Можно показать Toast или оставить без уведомления
                                })
                                .addOnFailureListener(e -> {
                                    // Логируем ошибку
                                });
                    }
                });
    }
}