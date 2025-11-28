package com.example.sfuzzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WordsFragment extends Fragment {

    private static final String ARG_TOPIC_NAME = "topic_name";
    private String topicName;

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

        TextView tvWordsContent = view.findViewById(R.id.tvWordsContent);

        // Подставляем слова в зависимости от темы
        tvWordsContent.setText(getWordsForTopic(topicName));

        return view;
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
