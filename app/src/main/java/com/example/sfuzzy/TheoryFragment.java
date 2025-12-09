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

        scrollView = (ScrollView) tvTheoryContent.getParent();

        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        loadTheory();

        Button btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private void loadTheory() {

        DatabaseRepository repository = new DatabaseRepository();

        repository.loadTheory(topicName, new DatabaseRepository.TheoryCallback() {
            @Override
            public void onSuccess(String theoryHtml) {
                progressBar.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);

                tvTheoryContent.setText(Html.fromHtml(theoryHtml, Html.FROM_HTML_MODE_LEGACY));

                new ProgressRepository().incrementLessonProgress();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                tvTheoryContent.setText("Ошибка загрузки: " + error);
            }
        });
    }
}
