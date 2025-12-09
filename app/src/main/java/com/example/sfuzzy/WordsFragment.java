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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Map;

public class WordsFragment extends Fragment {

    private static final String ARG_TOPIC_NAME = "topic_name";
    private String topicName;

    private TextView wordLabel, feedbackLabel;
    private EditText inputField;
    private Button submitButton, backButton;
    private ProgressBar progressBar;
    private LinearLayout contentLayout;

    private WordsQuizManager quizManager;

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

        // Инициализация менеджера
        quizManager = new WordsQuizManager(requireContext(), new WordsQuizManager.WordsQuizCallback() {
            @Override
            public void onWordsLoaded(Map<String, String> words) {
                // Можно добавить дополнительную логику
            }

            @Override
            public void onError(String error) {
                feedbackLabel.setText("Ошибка загрузки: " + error);
            }

            @Override
            public void onQuizCompleted(int totalWords) {
                inputField.setEnabled(false);
                submitButton.setEnabled(false);
                new ProgressRepository().incrementLessonProgress();
            }
        });

        quizManager.setupViews(wordLabel, feedbackLabel, inputField, progressBar, contentLayout);

        // Загрузка слов
        quizManager.loadWords(topicName);

        submitButton.setOnClickListener(v -> quizManager.checkTranslation());

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }
}