package com.example.sfuzzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class WordsFragment extends Fragment {

    private static final String ARG_TOPIC_NAME = "topic_name";
    private Map<String, String> wordMap = new LinkedHashMap<>();
    private List<String> englishWords = new ArrayList<>();
    private int currentIndex = 0;
    private String topicName;
    private TextView wordLabel, feedbackLabel;
    private EditText inputField;
    private Button submitButton, backButton;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;

    private FirebaseDatabase db = FirebaseDatabase.getInstance("https://sfuzzy-93892-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private DatabaseReference dbRef;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    public static WordsFragment newInstance(String topicName) {
        WordsFragment fragment = new WordsFragment();
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
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_words, container, false);

        wordLabel = view.findViewById(R.id.wordLabel);
        inputField = view.findViewById(R.id.inputField);
        submitButton = view.findViewById(R.id.submitButton);
        backButton = view.findViewById(R.id.backButton);
        feedbackLabel = view.findViewById(R.id.feedbackLabel);
        progressBar = view.findViewById(R.id.progressBar);
        contentLayout = view.findViewById(R.id.contentLayout);

        // Показываем прогресс бар, скрываем контент
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        dbRef = db.getReference("topics").child(topicName).child("words");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                wordMap.clear();
                englishWords.clear();

                for (DataSnapshot wordSnap : snapshot.getChildren()) {
                    String englishWord = wordSnap.getKey();
                    List<String> translations = new ArrayList<>();

                    for (DataSnapshot t : wordSnap.getChildren()) {
                        String tr = t.getValue(String.class);
                        if (tr != null) translations.add(tr);
                    }

                    if (englishWord != null && !translations.isEmpty()) {
                        wordMap.put(englishWord, String.join(", ", translations));
                    }
                }

                englishWords.addAll(wordMap.keySet());
                Collections.shuffle(englishWords);
                currentIndex = 0;

                // Скрываем прогресс бар, показываем контент
                progressBar.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);

                if (!englishWords.isEmpty()) {
                    loadNextWord();
                } else {
                    wordLabel.setText("Слова не найдены");
                    feedbackLabel.setText("Попробуйте снова");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
                feedbackLabel.setText("Ошибка загрузки: " + error.getMessage());
            }
        });

        submitButton.setOnClickListener(v -> checkTranslation());

        return view;
    }

    private void loadNextWord() {
        if (englishWords.isEmpty()) {
            wordLabel.setText("Слова не загружены");
            feedbackLabel.setText("Попробуйте снова");
            return;
        }

        if (currentIndex < englishWords.size()) {
            String word = englishWords.get(currentIndex);
            wordLabel.setText("Переведите: " + word);
            inputField.setText("");
            inputField.requestFocus();
            feedbackLabel.setText("Слово " + (currentIndex + 1) + " из " + englishWords.size());
        } else {
            // Все слова пройдены
            wordLabel.setText("Поздравляем! Все слова пройдены!");
            inputField.setEnabled(false);
            submitButton.setEnabled(false);
            feedbackLabel.setText("Вы завершили все " + englishWords.size() + " слов!");
            updateProgress(); // обновляем прогресс
        }
    }

    private void checkTranslation() {
        String userInput = inputField.getText().toString().trim().toLowerCase(Locale.ROOT);
        if (userInput.isEmpty()) {
            Toast.makeText(getContext(), "Введите перевод", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentIndex >= englishWords.size()) return;

        String word = englishWords.get(currentIndex);
        String correctTranslations = wordMap.get(word);

        if (correctTranslations == null) return;

        String[] possibleAnswers = correctTranslations.toLowerCase(Locale.ROOT).split(",\\s*");

        boolean isCorrect = false;
        for (String answer : possibleAnswers) {
            if (userInput.equals(answer.trim())) {
                isCorrect = true;
                break;
            }
        }

        if (isCorrect) {
            feedbackLabel.setText("Верно! ✅");
            currentIndex++;
            // Небольшая задержка перед следующим словом
            inputField.postDelayed(() -> loadNextWord(), 800);
        } else {
            feedbackLabel.setText("Неверно ❌\nПравильно: " + correctTranslations);
        }
    }

    private void updateProgress() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        firestore.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> progress = (Map<String, Object>) documentSnapshot.get("progress");
                        if (progress == null) progress = new HashMap<>();

                        long lessonsCompleted = ((Number) progress.getOrDefault("lessonsCompleted", 0)).longValue();
                        lessonsCompleted++; // увеличиваем на 1

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("progress.lessonsCompleted", lessonsCompleted);

                        firestore.collection("users").document(uid)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Прогресс обновлён!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Ошибка обновления прогресса", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }
}