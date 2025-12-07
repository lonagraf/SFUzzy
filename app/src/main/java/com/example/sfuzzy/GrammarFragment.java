package com.example.sfuzzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrammarFragment extends Fragment {

    private static final String ARG_TOPIC_NAME = "topic_name";
    private String topicName;

    private TextView tvQuestion;
    private RadioGroup rgAnswers;
    private Button btnCheck, btnBackToMenu;
    private ProgressBar progressBar;

    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    public static GrammarFragment newInstance(String topicName) {
        GrammarFragment fragment = new GrammarFragment();
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

        View view = inflater.inflate(R.layout.fragment_grammar, container, false);

        tvQuestion = view.findViewById(R.id.tvQuestion);
        rgAnswers = view.findViewById(R.id.rgAnswers);
        btnCheck = view.findViewById(R.id.btnCheck);
        btnBackToMenu = view.findViewById(R.id.btnBackToMenu);
        progressBar = view.findViewById(R.id.progressBar);

        // Скрываем вопросы и кнопку до загрузки
        tvQuestion.setVisibility(View.GONE);
        rgAnswers.setVisibility(View.GONE);
        btnCheck.setVisibility(View.GONE);
        btnBackToMenu.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        loadQuestionsFromFirebase();

        btnCheck.setOnClickListener(v -> checkAnswer());

        btnBackToMenu.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .popBackStack());

        return view;
    }

    private void loadQuestionsFromFirebase() {
        DatabaseRepository repository = new DatabaseRepository();

        repository.loadGrammar(topicName, new DatabaseRepository.GrammarCallback() {
            @Override
            public void onSuccess(List<Question> loadedQuestions) {
                progressBar.setVisibility(View.GONE);

                questions.clear();
                questions.addAll(loadedQuestions);

                if (!questions.isEmpty()) {
                    tvQuestion.setVisibility(View.VISIBLE);
                    rgAnswers.setVisibility(View.VISIBLE);
                    btnCheck.setVisibility(View.VISIBLE);

                    currentQuestionIndex = 0;
                    score = 0;

                    displayQuestion(currentQuestionIndex);
                } else {
                    tvQuestion.setText("Вопросы отсутствуют.");
                    tvQuestion.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Ошибка загрузки: " + error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void displayQuestion(int index) {
        if (index >= questions.size()) {
            showFinalScore();
            return;
        }

        Question q = questions.get(index);

        tvQuestion.setText(q.question);
        rgAnswers.removeAllViews();

        for (String option : q.options) {
            RadioButton rb = new RadioButton(getContext());
            rb.setText(option);
            rgAnswers.addView(rb);
        }
    }

    private void checkAnswer() {
        int selectedId = rgAnswers.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(requireContext(), "Выберите вариант ответа", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rb = rgAnswers.findViewById(selectedId);
        if (rb == null) {
            Toast.makeText(requireContext(), "Ошибка выбора ответа", Toast.LENGTH_SHORT).show();
            return;
        }

        String chosen = rb.getText().toString();
        Question q = questions.get(currentQuestionIndex);

        if (chosen.equals(q.correctAnswer)) {
            score++;
            Toast.makeText(requireContext(), "Верно!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(),
                    "Неверно. Правильный ответ: " + q.correctAnswer,
                    Toast.LENGTH_SHORT).show();
        }

        currentQuestionIndex++;

        if (currentQuestionIndex < questions.size()) {
            displayQuestion(currentQuestionIndex);
        } else {
            showFinalScore();
        }
    }

    private void showFinalScore() {
        int total = questions.size();

        tvQuestion.setText("Тест завершён!\n\nВаш результат: " + score + " из " + total);

        rgAnswers.removeAllViews();
        btnCheck.setVisibility(View.GONE);
        btnBackToMenu.setVisibility(View.VISIBLE);

        new ProgressRepository().incrementLessonProgress();
    }



    static class Question {
        String question;
        List<String> options;
        String correctAnswer;

        Question(String question, List<String> options, String correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }
}
