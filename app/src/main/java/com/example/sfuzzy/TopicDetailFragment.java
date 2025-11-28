package com.example.sfuzzy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TopicDetailFragment extends Fragment {

    private static final String ARG_TOPIC_NAME = "topic_name";
    private String topicName;
    private Button btnBack;

    public static TopicDetailFragment newInstance(String topicName) {
        TopicDetailFragment fragment = new TopicDetailFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic_detail, container, false);

        TextView tvTopicTitle = view.findViewById(R.id.tvTopicTitle);
        tvTopicTitle.setText(topicName);

        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_view_tag, new TopicsFragment())
                    .addToBackStack(null)
                    .commit();
        });


        // Здесь можно добавить условие для показа конкретного контента
        // if (topicName.equals("Basics")) { ... }

        return view;
    }
}
