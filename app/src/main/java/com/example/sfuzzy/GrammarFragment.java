package com.example.sfuzzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class GrammarFragment extends Fragment {

    private static final String ARG_TOPIC_NAME = "topic_name";
    private String topicName;

    private TextView tvQuestion;
    private RadioGroup rgAnswers;
    private Button btnCheck, btnBackToMenu, btnBack;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;

    private GrammarQuizManager quizManager;

    // Вложенный класс Question - ДОЛЖЕН БЫТЬ public static
    public static class Question {
        public String question;
        public List<String> options;
        public String correctAnswer;

        // Пустой конструктор для Firebase
        public Question() {
        }

        public Question(String question, List<String> options, String correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

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
        btnBack = view.findViewById(R.id.btnBack);
        progressBar = view.findViewById(R.id.progressBar);
        contentLayout = view.findViewById(R.id.contentLayout);

        // Инициализация менеджера теста
        quizManager = new GrammarQuizManager(requireContext(), new GrammarQuizManager.QuizCallback() {
            @Override
            public void onQuestionsLoaded(List<Question> questions) {
                // Можно добавить дополнительную логику при загрузке
            }

            @Override
            public void onError(String error) {
                tvQuestion.setText(error);
                tvQuestion.setVisibility(View.VISIBLE);
            }

            @Override
            public void onQuizCompleted(int score, int total) {
                btnCheck.setVisibility(View.GONE);
                btnBackToMenu.setVisibility(View.VISIBLE);
                new ProgressRepository().incrementLessonProgress();
            }
        });

        quizManager.setupViews(tvQuestion, rgAnswers, progressBar, contentLayout);

        // Загрузка вопросов
        quizManager.loadQuestions(topicName);

        btnCheck.setOnClickListener(v -> quizManager.checkAnswer());

        btnBackToMenu.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .popBackStack());

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager()
                .popBackStack());

        return view;
    }
}