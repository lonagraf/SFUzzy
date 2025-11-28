package com.example.sfuzzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GrammarFragment extends Fragment {

    private static final String ARG_TOPIC_NAME = "topic_name";
    private String topicName;

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

        TextView tvGrammarContent = view.findViewById(R.id.tvGrammarContent);

        // Подставляем грамматику в зависимости от темы
        tvGrammarContent.setText(getGrammarTextForTopic(topicName));

        return view;
    }

    private String getGrammarTextForTopic(String topic) {
        switch (topic) {
            case "Basics":
                return "Грамматика по теме Basics...";
            case "Family":
                return "Грамматика по теме Family...";
            case "Food":
                return "Грамматика по теме Food...";
            case "Travel":
                return "Грамматика по теме Travel...";
            default:
                return "Грамматика недоступна";
        }
    }
}
