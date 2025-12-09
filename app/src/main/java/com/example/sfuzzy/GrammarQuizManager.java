package com.example.sfuzzy;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class GrammarQuizManager {

    public interface QuizCallback {
        void onQuestionsLoaded(List<GrammarFragment.Question> questions);
        void onError(String error);
        void onQuizCompleted(int score, int total);
    }

    private Context context;
    private List<GrammarFragment.Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private QuizCallback callback;

    private TextView tvQuestion;
    private RadioGroup rgAnswers;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;

    private boolean isLoaded = false;

    public GrammarQuizManager(Context context, QuizCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void setupViews(TextView tvQuestion, RadioGroup rgAnswers,
                           ProgressBar progressBar, LinearLayout contentLayout) {
        this.tvQuestion = tvQuestion;
        this.rgAnswers = rgAnswers;
        this.progressBar = progressBar;
        this.contentLayout = contentLayout;
    }

    public void loadQuestions(String topicName) {
        if (topicName == null || topicName.isEmpty()) {
            callback.onError("Тема не выбрана");
            return;
        }

        showLoading(true);

        DatabaseRepository repository = new DatabaseRepository();
        repository.loadGrammar(topicName, new DatabaseRepository.GrammarCallback() { // ← исправлено здесь
            @Override
            public void onSuccess(List<GrammarFragment.Question> loadedQuestions) {
                showLoading(false);
                questions = loadedQuestions;
                isLoaded = true;
                currentQuestionIndex = 0;
                score = 0;
                callback.onQuestionsLoaded(loadedQuestions);
                if (!loadedQuestions.isEmpty()) {
                    displayQuestion(currentQuestionIndex);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                callback.onError("Ошибка загрузки: " + error);
            }
        });
    }

    public void checkAnswer() {
        if (!isLoaded || questions == null || questions.isEmpty()) {
            Toast.makeText(context, "Вопросы не загружены", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = rgAnswers.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(context, "Выберите вариант ответа", Toast.LENGTH_SHORT).show();
            return;
        }

        GrammarFragment.Question currentQuestion = questions.get(currentQuestionIndex);
        String chosen = ((android.widget.RadioButton) rgAnswers.findViewById(selectedId)).getText().toString();

        if (chosen.equals(currentQuestion.correctAnswer)) {
            score++;
            Toast.makeText(context, "Верно!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Неверно. Правильный ответ: " + currentQuestion.correctAnswer,
                    Toast.LENGTH_LONG).show();
        }

        currentQuestionIndex++;

        if (currentQuestionIndex < questions.size()) {
            displayQuestion(currentQuestionIndex);
        } else {
            completeQuiz();
        }
    }

    private void displayQuestion(int index) {
        if (index >= questions.size()) {
            completeQuiz();
            return;
        }

        GrammarFragment.Question q = questions.get(index);
        tvQuestion.setText(q.question);
        rgAnswers.removeAllViews();

        for (String option : q.options) {
            android.widget.RadioButton rb = new android.widget.RadioButton(context);
            rb.setText(option);
            rb.setTextSize(16f);
            rb.setPadding(8, 12, 8, 12);
            rgAnswers.addView(rb);
        }
    }

    private void completeQuiz() {
        int total = questions.size();
        tvQuestion.setText("Тест завершён!\n\nВаш результат: " + score + " из " + total);
        rgAnswers.removeAllViews();
        callback.onQuizCompleted(score, total);
    }

    private void showLoading(boolean show) {
        if (progressBar != null && contentLayout != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public boolean hasNextQuestion() {
        return isLoaded && currentQuestionIndex < questions.size();
    }

    public void reset() {
        currentQuestionIndex = 0;
        score = 0;
        if (questions != null && !questions.isEmpty()) {
            displayQuestion(0);
        }
    }
}