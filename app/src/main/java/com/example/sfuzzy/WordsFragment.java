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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        progressBar = view.findViewById(R.id.progressBar);
        contentLayout = view.findViewById(R.id.contentLayout);

        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);

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

                progressBar.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);

                currentIndex = 0;

                if (!englishWords.isEmpty()) {
                    loadNextWord();
                } else {
                    wordLabel.setText("Слова не найдены");
                    feedbackLabel.setText("Попробуйте снова");
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
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
            inputField.requestFocus();
            feedbackLabel.setText("Слово " + (currentIndex + 1) + " из " + englishWords.size());
        } else {
            wordLabel.setText("Поздравляем! Все слова пройдены!");
            inputField.setEnabled(false);
            submitButton.setEnabled(false);

            feedbackLabel.setText("Вы завершили все слова! (" + englishWords.size() + ")");

            new ProgressRepository().incrementLessonProgress();
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
            feedbackLabel.setText("Верно!");
            currentIndex++;
            inputField.postDelayed(this::loadNextWord, 800);
        } else {
            feedbackLabel.setText("Неверно \nПравильно: " + correctTranslations);
        }
    }
}
