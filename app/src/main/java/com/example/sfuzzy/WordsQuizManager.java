package com.example.sfuzzy;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WordsQuizManager {

    public interface WordsQuizCallback {
        void onWordsLoaded(Map<String, String> words);
        void onError(String error);
        void onQuizCompleted(int totalWords);
    }

    private Context context;
    private Map<String, String> wordMap = new LinkedHashMap<>();
    private List<String> englishWords = new ArrayList<>();
    private int currentIndex = 0;
    private WordsQuizCallback callback;

    private TextView wordLabel, feedbackLabel;
    private EditText inputField;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;

    private boolean isLoaded = false;

    public WordsQuizManager(Context context, WordsQuizCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void setupViews(TextView wordLabel, TextView feedbackLabel, EditText inputField,
                           ProgressBar progressBar, LinearLayout contentLayout) {
        this.wordLabel = wordLabel;
        this.feedbackLabel = feedbackLabel;
        this.inputField = inputField;
        this.progressBar = progressBar;
        this.contentLayout = contentLayout;
    }

    public void loadWords(String topicName) {
        if (topicName == null || topicName.isEmpty()) {
            callback.onError("Тема не выбрана");
            return;
        }

        showLoading(true);

        DatabaseRepository repository = new DatabaseRepository();
        repository.loadWords(topicName, new DatabaseRepository.WordsCallback() {
            @Override
            public void onSuccess(Map<String, String> words) {
                showLoading(false);
                wordMap.clear();
                wordMap.putAll(words);

                englishWords.clear();
                englishWords.addAll(wordMap.keySet());
                Collections.shuffle(englishWords);

                isLoaded = true;
                currentIndex = 0;
                callback.onWordsLoaded(words);

                if (!englishWords.isEmpty()) {
                    loadNextWord();
                } else {
                    wordLabel.setText("Слова не найдены");
                    feedbackLabel.setText("Попробуйте снова");
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                callback.onError("Ошибка загрузки: " + error);
            }
        });
    }

    public void checkTranslation() {
        if (!isLoaded || englishWords.isEmpty()) {
            Toast.makeText(context, "Слова не загружены", Toast.LENGTH_SHORT).show();
            return;
        }

        String userInput = inputField.getText().toString().trim().toLowerCase(Locale.ROOT);
        if (userInput.isEmpty()) {
            Toast.makeText(context, "Введите перевод", Toast.LENGTH_SHORT).show();
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

            // Автоматический переход к следующему слову
            if (currentIndex < englishWords.size()) {
                inputField.postDelayed(this::loadNextWord, 800);
            } else {
                completeQuiz();
            }
        } else {
            feedbackLabel.setText("Неверно \nПравильно: " + correctTranslations);
        }
    }

    private void loadNextWord() {
        if (currentIndex < englishWords.size()) {
            String word = englishWords.get(currentIndex);
            wordLabel.setText("Переведите: " + word);
            inputField.setText("");
            inputField.requestFocus();
            feedbackLabel.setText("Слово " + (currentIndex + 1) + " из " + englishWords.size());
        } else {
            completeQuiz();
        }
    }

    private void completeQuiz() {
        int total = englishWords.size();
        wordLabel.setText("Поздравляем! Все слова пройдены!");
        feedbackLabel.setText("Вы завершили все слова! (" + total + ")");
        inputField.setEnabled(false);
        callback.onQuizCompleted(total);
    }

    private void showLoading(boolean show) {
        if (progressBar != null && contentLayout != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}