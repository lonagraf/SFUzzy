package com.example.sfuzzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class WordsFragment extends Fragment {

    private static final String ARG_TOPIC_NAME = "topic_name";
    private Map<String, String> wordMap = new LinkedHashMap<>();
    private List<String> englishWords = new ArrayList<>();
    private int currentIndex = 0;
    private String topicName;
    private TextView wordLabel, feedbackLabel;
    private EditText inputField;
    private Button submitButton;

    FirebaseDatabase db = FirebaseDatabase.getInstance("https://sfuzzy-93892-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference dbRef;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_words, container, false);

        wordLabel = view.findViewById(R.id.wordLabel);
        inputField = view.findViewById(R.id.inputField);
        submitButton = view.findViewById(R.id.submitButton);
        feedbackLabel = view.findViewById(R.id.feedbackLabel);
        dbRef = db.getReference("topics")
                .child(topicName)
                .child("words");

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
                loadNextWord();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                feedbackLabel.setText("Ошибка загрузки: " + error.getMessage());
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTranslation();
            }
        });

        return view;
    }




    private boolean isLikelyWordMap(Map<?, ?> map) {
        // Проверяем, похоже ли это на словарь слов
        if (map.isEmpty()) return false;

        for (Object key : map.keySet()) {
            if (key instanceof String) {
                String strKey = (String) key;
                // Если ключ - английское слово (только буквы), вероятно это наш словарь
                if (strKey.matches("[a-zA-Z]+")) {
                    return true;
                }
            }
        }
        return false;
    }




    private void loadNextWord() {
        System.out.println("Loading next word. Current index: " + currentIndex + ", Total: " + englishWords.size());

        if (englishWords.isEmpty()) {
            wordLabel.setText("Слова не загружены");
            feedbackLabel.setText("Попробуйте снова");
            return;
        }

        if (currentIndex < englishWords.size()) {
            String word = englishWords.get(currentIndex);
            wordLabel.setText("Переведите: " + word);
            inputField.setText("");
            feedbackLabel.setText("Слово " + (currentIndex + 1) + " из " + englishWords.size());
        } else {
            wordLabel.setText("Поздравляем!");
            inputField.setEnabled(false);
            submitButton.setEnabled(false);
            feedbackLabel.setText("Вы завершили все слова! (" + englishWords.size() + " слов)");
        }
    }

    private void checkTranslation() {
        String userInput = inputField.getText().toString().trim().toLowerCase(Locale.ROOT);
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
            feedbackLabel.setText("Верно!");
            currentIndex++;
            loadNextWord();
        } else {
            feedbackLabel.setText("Неправильно. Возможные ответы: " + correctTranslations);
        }
    }



    private String getWordsForTopic(String topic) {
        switch (topic) {
            case "Basics":
                return "Слова по теме Basics...";
            case "Family":
                return "Слова по теме Family...";
            case "Food":
                return "Слова по теме Food...";
            case "Travel":
                return "Слова по теме Travel...";
            default:
                return "Слова недоступны";
        }
    }
}
