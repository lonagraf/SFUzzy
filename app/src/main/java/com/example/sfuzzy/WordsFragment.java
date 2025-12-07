package com.example.sfuzzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        backButton = view.findViewById(R.id.backButton);
        feedbackLabel = view.findViewById(R.id.feedbackLabel);

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        DatabaseRepository repository = new DatabaseRepository();

        repository.loadWords(topicName, new DatabaseRepository.WordsCallback() {
            @Override
            public void onSuccess(Map<String, String> words) {
                wordMap.clear();
                wordMap.putAll(words);

                englishWords.clear();
                englishWords.addAll(wordMap.keySet());

                Collections.shuffle(englishWords);
                currentIndex = 0;

                loadNextWord();
            }

            @Override
            public void onError(String error) {
                feedbackLabel.setText("Ошибка загрузки: " + error);
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
            feedbackLabel.setText("Слово " + (currentIndex + 1) + " из " + englishWords.size());
        } else {
            // Все слова пройдены
            wordLabel.setText("Поздравляем!");
            inputField.setEnabled(false);
            submitButton.setEnabled(false);
            feedbackLabel.setText("Вы завершили все слова! (" + englishWords.size() + " слов)");
            new ProgressRepository().incrementLessonProgress();
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
            loadNextWord(); // прогресс обновится только после конца
        } else {
            feedbackLabel.setText("Неправильно. Возможные ответы: " + correctTranslations);
        }
    }


}
